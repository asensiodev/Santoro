package com.asensiodev.santoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.repository.MovieMutationRepository
import com.asensiodev.core.domain.repository.SyncScheduler
import com.asensiodev.core.domain.result.rethrowCancellation
import com.asensiodev.core.domain.usecase.ObserveHasSeenGuestOnboardingUseCase
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetHasSeenGuestOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
    @Inject
    constructor(
        observeAuthStateUseCase: ObserveAuthStateUseCase,
        observeHasSeenGuestOnboardingUseCase: ObserveHasSeenGuestOnboardingUseCase,
        observeThemeUseCase: ObserveThemeUseCase,
        private val setHasSeenGuestOnboardingUseCase: SetHasSeenGuestOnboardingUseCase,
        private val syncScheduler: SyncScheduler,
        private val movieMutationRepository: MovieMutationRepository,
        private val deletionRecoveryRepository: AccountDeletionRecoveryRepository,
    ) : ViewModel() {
        private val sessionRequests = MutableSharedFlow<SantoroUser?>(replay = 1)
        private var previousUid: String? = null
        private var transitionClearPending = false
        private var localDeletionCleanupPending = false
        private var remoteDeletionInFlight = false
        private var deletionRecoveryFailed = false
        private var deletionJob: Job? = null
        private val hasSeenGuestOnboarding =
            observeHasSeenGuestOnboardingUseCase()
                .stateIn(viewModelScope, SharingStarted.Eagerly, false)
        private val _uiState = MutableStateFlow<MainActivityUiState>(MainActivityUiState.Loading)
        val uiState: StateFlow<MainActivityUiState> = _uiState.asStateFlow()
        private val _isRemoteAccountDeletionInProgress = MutableStateFlow(false)
        val isRemoteAccountDeletionInProgress: StateFlow<Boolean> =
            _isRemoteAccountDeletionInProgress.asStateFlow()

        val themeOption: StateFlow<ThemeOption> =
            observeThemeUseCase()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = ThemeOption.SYSTEM,
                )

        init {
            viewModelScope.launch {
                sessionRequests.collectLatest(::enterSession)
            }
            viewModelScope.launch {
                observeAuthStateUseCase().collect(::onAuthChanged)
            }
            viewModelScope.launch {
                hasSeenGuestOnboarding.collect(::onGuestOnboardingChanged)
            }
            viewModelScope.launch {
                deletionRecoveryRepository.isLocalCleanupPending.collect { pending ->
                    val wasPending = localDeletionCleanupPending
                    localDeletionCleanupPending = pending
                    val deletionLeaseActive =
                        remoteDeletionInFlight ||
                            deletionRecoveryRepository.isRemoteDeletionInFlight.first()
                    if (pending && !deletionLeaseActive) {
                        preemptForAccountDeletion()
                        startAccountDeletionRecovery()
                    } else if (wasPending && !remoteDeletionInFlight) {
                        requestLatestSession()
                    }
                }
            }
            viewModelScope.launch {
                deletionRecoveryRepository.isRemoteDeletionInFlight.collect { inFlight ->
                    val wasInFlight = remoteDeletionInFlight
                    remoteDeletionInFlight = inFlight
                    _isRemoteAccountDeletionInProgress.value = inFlight
                    if (!inFlight) {
                        val cleanupPending =
                            deletionRecoveryRepository.isLocalCleanupPending.first()
                        localDeletionCleanupPending = cleanupPending
                        if (cleanupPending && wasInFlight) {
                            preemptForAccountDeletion()
                            startAccountDeletionRecovery()
                        } else if (
                            wasInFlight &&
                            _uiState.value !is MainActivityUiState.Authenticated
                        ) {
                            requestLatestSession()
                        }
                    }
                }
            }
        }

        fun process(intent: MainActivityIntent) {
            when (intent) {
                MainActivityIntent.RetryAccountRecovery -> retryAccountRecovery()
                MainActivityIntent.DismissGuestOnboarding -> dismissGuestOnboarding()
            }
        }

        private suspend fun onAuthChanged(user: SantoroUser?) {
            val uidChanged = previousUid != null && previousUid != user?.uid
            if (user == null || uidChanged) {
                transitionClearPending = true
            }
            previousUid = user?.uid
            if (!updateAuthenticatedInPlace(user)) {
                _uiState.value = MainActivityUiState.Loading
                sessionRequests.emit(user)
            }
        }

        @Suppress("ReturnCount")
        private suspend fun enterSession(user: SantoroUser?) {
            if (deletionPendingNow()) {
                if (deletionRecoveryFailed) {
                    _uiState.value = MainActivityUiState.AccountDeletionRecoveryError
                }
                return
            }
            _uiState.value = MainActivityUiState.Loading
            val clearMovies = user == null || transitionClearPending
            if (clearMovies && !clearMovies()) return
            if (!hasSessionAuthority(user)) return
            if (user == null) {
                _uiState.value = MainActivityUiState.Unauthenticated
                return
            }
            transitionClearPending = false
            scheduleSessionSync(user.uid)
            if (!hasSessionAuthority(user)) return
            _uiState.value =
                MainActivityUiState.Authenticated(
                    user = user,
                    showGuestOnboarding = showOnboarding(user),
                )
        }

        private suspend fun clearMovies(): Boolean {
            val result = runCatching { movieMutationRepository.clearMovies() }.rethrowCancellation()
            if (result.isFailure) {
                _uiState.value = MainActivityUiState.LocalCleanupError
            }
            return result.isSuccess
        }

        private suspend fun scheduleSessionSync(expectedUid: String) {
            if (!hasSessionAuthority(expectedUid)) return
            try {
                syncScheduler.schedulePeriodicSync()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                Unit
            }
            if (!hasSessionAuthority(expectedUid)) return
            try {
                syncScheduler.scheduleImmediateSync()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                Unit
            }
        }

        private fun retryAccountRecovery() {
            when (_uiState.value) {
                MainActivityUiState.AccountDeletionRecoveryError -> startAccountDeletionRecovery()
                MainActivityUiState.LocalCleanupError -> requestLatestSession()
                else -> Unit
            }
        }

        private fun startAccountDeletionRecovery() {
            if (deletionJob?.isActive == true) return
            deletionRecoveryFailed = false
            deletionJob = viewModelScope.launch { recoverAccountDeletion() }
        }

        @Suppress("ReturnCount")
        private suspend fun recoverAccountDeletion() {
            _uiState.value = MainActivityUiState.Loading
            deletionRecoveryRepository.isRemoteDeletionInFlight.filterNot { it }.first()
            if (!deletionRecoveryRepository.isLocalCleanupPending.first()) return
            val cleanup =
                runCatching {
                    movieMutationRepository.clearMovies()
                }.rethrowCancellation()
            if (cleanup.isFailure) {
                deletionRecoveryFailed = true
                _uiState.value = MainActivityUiState.AccountDeletionRecoveryError
                return
            }
            deletionRecoveryRepository.isRemoteDeletionInFlight.filterNot { it }.first()
            if (!deletionRecoveryRepository.isLocalCleanupPending.first()) return
            val markerClear =
                deletionRecoveryRepository.clearLocalCleanupPending().rethrowCancellation()
            if (markerClear.isFailure) {
                deletionRecoveryFailed = true
                _uiState.value = MainActivityUiState.AccountDeletionRecoveryError
            }
        }

        private fun dismissGuestOnboarding() {
            val state = _uiState.value
            if (state is MainActivityUiState.Authenticated) {
                _uiState.value = state.copy(showGuestOnboarding = false)
            }
            viewModelScope.launch { setHasSeenGuestOnboardingUseCase(true) }
        }

        private fun onGuestOnboardingChanged(hasSeen: Boolean) {
            val state = _uiState.value as? MainActivityUiState.Authenticated ?: return
            if (!state.user.isAnonymous) return
            _uiState.value = state.copy(showGuestOnboarding = !hasSeen)
        }

        private fun preemptForAccountDeletion() {
            _uiState.value = MainActivityUiState.Loading
            requestLatestSession()
        }

        private fun requestLatestSession() {
            if (sessionRequests.replayCache.isNotEmpty()) {
                sessionRequests.tryEmit(sessionRequests.replayCache.last())
            }
        }

        @Suppress("ReturnCount")
        private suspend fun updateAuthenticatedInPlace(user: SantoroUser?): Boolean {
            if (user == null || transitionClearPending) return false
            val state = _uiState.value as? MainActivityUiState.Authenticated ?: return false
            if (state.user.uid != user.uid) return false
            val deletionLeaseActive =
                remoteDeletionInFlight ||
                    deletionRecoveryRepository.isRemoteDeletionInFlight.first()
            val cleanupPending =
                localDeletionCleanupPending ||
                    deletionRecoveryRepository.isLocalCleanupPending.first()
            if (cleanupPending && !deletionLeaseActive) return false
            _uiState.value =
                state.copy(
                    user = user,
                    showGuestOnboarding = showOnboarding(user),
                )
            return true
        }

        private suspend fun deletionPendingNow(): Boolean =
            localDeletionCleanupPending ||
                remoteDeletionInFlight ||
                deletionRecoveryRepository.isLocalCleanupPending.first() ||
                deletionRecoveryRepository.isRemoteDeletionInFlight.first()

        private suspend fun hasSessionAuthority(expectedUser: SantoroUser?): Boolean =
            latestUser()?.uid == expectedUser?.uid && !deletionPendingNow()

        private suspend fun hasSessionAuthority(expectedUid: String): Boolean =
            latestUser()?.uid == expectedUid && !deletionPendingNow()

        private fun latestUser(): SantoroUser? = sessionRequests.replayCache.lastOrNull()

        private fun showOnboarding(user: SantoroUser): Boolean =
            user.isAnonymous && !hasSeenGuestOnboarding.value
    }

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Authenticated(
        val user: SantoroUser,
        val showGuestOnboarding: Boolean,
    ) : MainActivityUiState
    data object Unauthenticated : MainActivityUiState
    data object LocalCleanupError : MainActivityUiState
    data object AccountDeletionRecoveryError : MainActivityUiState
}

sealed interface MainActivityIntent {
    data object RetryAccountRecovery : MainActivityIntent
    data object DismissGuestOnboarding : MainActivityIntent
}
