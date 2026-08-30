package com.asensiodev.santoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.result.rethrowCancellation
import com.asensiodev.core.domain.usecase.ObserveHasSeenGuestOnboardingUseCase
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetHasSeenGuestOnboardingUseCase
import com.asensiodev.library.observability.api.NoOpObservabilityTracker
import com.asensiodev.library.observability.api.ObservabilityTracker
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
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
        private val syncScheduler: WorkManagerSyncScheduler,
        private val recoveryRepository: AccountDeletionRecoveryRepository,
        private val databaseRepository: DatabaseRepository,
        private val observabilityTracker: ObservabilityTracker = NoOpObservabilityTracker,
    ) : ViewModel() {
        private val authFlow = observeAuthStateUseCase()
        private val recoveryState =
            MutableStateFlow<AccountDeletionRecoveryState>(AccountDeletionRecoveryState.Checking)

        val uiState: StateFlow<MainActivityUiState> =
            combine(
                authFlow,
                observeHasSeenGuestOnboardingUseCase(),
                recoveryRepository.isLocalCleanupPending,
                recoveryState,
            ) { user, hasSeenGuestOnboarding, isCleanupPending, recovery ->
                when {
                    recovery is AccountDeletionRecoveryState.Checking ||
                        recovery is AccountDeletionRecoveryState.Recovering ->
                        MainActivityUiState.Loading
                    recovery is AccountDeletionRecoveryState.Error ->
                        MainActivityUiState.AccountDeletionRecoveryError
                    isCleanupPending && user == null -> MainActivityUiState.Loading
                    user != null ->
                        MainActivityUiState.Authenticated(
                            showGuestOnboarding = user.isAnonymous && !hasSeenGuestOnboarding,
                        )
                    else -> MainActivityUiState.Unauthenticated
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MainActivityUiState.Loading,
            )

        val themeOption: StateFlow<ThemeOption> =
            observeThemeUseCase()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = ThemeOption.SYSTEM,
                )

        init {
            observePendingLocalCleanup()

            combine(
                authFlow,
                recoveryRepository.isLocalCleanupPending,
                recoveryState,
            ) { user, isCleanupPending, recovery ->
                user?.uid?.takeIf {
                    !isCleanupPending && recovery is AccountDeletionRecoveryState.Ready
                }
            }.mapNotNull { uid -> uid }
                .distinctUntilChanged()
                .onEach { uid ->
                    try {
                        observabilityTracker.trackAction(
                            SYNC_SCHEDULED,
                            mapOf(
                                USER_ID_PRESENT to uid.isNotBlank().toString(),
                            ),
                        )
                        syncScheduler.schedulePeriodicSync()
                        syncScheduler.scheduleImmediateSync()
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: IllegalStateException) {
                        observabilityTracker.recordError(SYNC_SCHEDULING_FAILED, exception)
                    }
                }.launchIn(viewModelScope)
        }

        fun retryAccountDeletionRecovery() {
            if (recoveryState.value !is AccountDeletionRecoveryState.Error) return
            viewModelScope.launch { recoverPendingLocalCleanup() }
        }

        fun dismissGuestOnboarding() {
            observabilityTracker.trackAction(GUEST_ONBOARDING_DISMISSED)
            viewModelScope.launch {
                setHasSeenGuestOnboardingUseCase(true)
            }
        }

        private fun observePendingLocalCleanup() {
            viewModelScope.launch {
                val pendingAtStartup = recoveryRepository.isLocalCleanupPending.first()
                if (pendingAtStartup) {
                    recoverPendingLocalCleanup()
                } else {
                    recoveryState.value = AccountDeletionRecoveryState.Ready
                }

                combine(
                    authFlow,
                    recoveryRepository.isLocalCleanupPending,
                ) { user, isCleanupPending ->
                    user == null && isCleanupPending
                }.drop(1)
                    .distinctUntilChanged()
                    .collect { shouldRecover ->
                        if (shouldRecover) recoverPendingLocalCleanup()
                    }
            }
        }

        private suspend fun recoverPendingLocalCleanup() {
            if (recoveryState.value is AccountDeletionRecoveryState.Recovering) return
            recoveryState.value = AccountDeletionRecoveryState.Recovering

            val cleanupResult = databaseRepository.clearAllUserData().rethrowCancellation()
            val result =
                if (cleanupResult.isSuccess) {
                    recoveryRepository.clearLocalCleanupPending().rethrowCancellation()
                } else {
                    cleanupResult
                }

            recoveryState.value =
                if (result.isSuccess) {
                    AccountDeletionRecoveryState.Ready
                } else {
                    AccountDeletionRecoveryState.Error
                }
        }

        private companion object {
            const val SYNC_SCHEDULED = "sync_scheduled"
            const val SYNC_SCHEDULING_FAILED = "sync_scheduling_failed"
            const val USER_ID_PRESENT = "user_id_present"
            const val GUEST_ONBOARDING_DISMISSED = "guest_onboarding_dismissed"
        }
    }

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Authenticated(
        val showGuestOnboarding: Boolean,
    ) : MainActivityUiState
    data object Unauthenticated : MainActivityUiState
    data object AccountDeletionRecoveryError : MainActivityUiState
}

private sealed interface AccountDeletionRecoveryState {
    data object Checking : AccountDeletionRecoveryState
    data object Ready : AccountDeletionRecoveryState
    data object Recovering : AccountDeletionRecoveryState
    data object Error : AccountDeletionRecoveryState
}
