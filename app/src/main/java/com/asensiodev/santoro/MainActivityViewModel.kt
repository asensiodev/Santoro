package com.asensiodev.santoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.core.domain.usecase.ObserveHasSeenGuestOnboardingUseCase
import com.asensiodev.core.domain.usecase.SetHasSeenGuestOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
    @Inject
    constructor(
        observeAuthStateUseCase: ObserveAuthStateUseCase,
        observeHasSeenGuestOnboardingUseCase: ObserveHasSeenGuestOnboardingUseCase,
        private val setHasSeenGuestOnboardingUseCase: SetHasSeenGuestOnboardingUseCase,
    ) : ViewModel() {
        val uiState: StateFlow<MainActivityUiState> =
            combine(
                observeAuthStateUseCase(),
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

        fun dismissGuestOnboarding() {
            viewModelScope.launch {
                setHasSeenGuestOnboardingUseCase(true)
            }
        }
    }

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Authenticated(
        val showGuestOnboarding: Boolean,
    ) : MainActivityUiState
    data object Unauthenticated : MainActivityUiState
}
