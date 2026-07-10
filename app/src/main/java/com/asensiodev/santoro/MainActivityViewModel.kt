package com.asensiodev.santoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.observability.NoOpObservabilityTracker
import com.asensiodev.core.domain.observability.ObservabilityTracker
import com.asensiodev.core.domain.usecase.ObserveHasSeenGuestOnboardingUseCase
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetHasSeenGuestOnboardingUseCase
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
        private val observabilityTracker: ObservabilityTracker = NoOpObservabilityTracker,
    ) : ViewModel() {
        private val authFlow = observeAuthStateUseCase()

        val uiState: StateFlow<MainActivityUiState> =
            combine(
                authFlow,
                observeHasSeenGuestOnboardingUseCase(),
            ) { user, hasSeenGuestOnboarding ->
                if (user != null) {
                    MainActivityUiState.Authenticated(
                        showGuestOnboarding = user.isAnonymous && !hasSeenGuestOnboarding,
                    )
                } else {
                    MainActivityUiState.Unauthenticated
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
            authFlow
                .mapNotNull { user -> user?.uid }
                .distinctUntilChanged()
                .onEach { uid ->
                    observabilityTracker.trackAction(
                        SYNC_SCHEDULED,
                        mapOf(
                            USER_ID_PRESENT to uid.isNotBlank().toString(),
                        ),
                    )
                    syncScheduler.schedulePeriodicSync()
                    syncScheduler.scheduleImmediateSync()
                }.launchIn(viewModelScope)
        }

        fun dismissGuestOnboarding() {
            observabilityTracker.trackAction(GUEST_ONBOARDING_DISMISSED)
            viewModelScope.launch {
                setHasSeenGuestOnboardingUseCase(true)
            }
        }

        private companion object {
            const val SYNC_SCHEDULED = "sync_scheduled"
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
}
