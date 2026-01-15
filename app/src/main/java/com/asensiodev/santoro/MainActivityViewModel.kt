package com.asensiodev.santoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
    @Inject
    constructor(
        observeAuthStateUseCase: ObserveAuthStateUseCase,
    ) : ViewModel() {
        val uiState: StateFlow<MainActivityUiState> =
            observeAuthStateUseCase()
                .map { user ->
                    if (user != null) {
                        MainActivityUiState.Authenticated
                    } else {
                        MainActivityUiState.Unauthenticated
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = MainActivityUiState.Loading,
                )
    }

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data object Authenticated : MainActivityUiState
    data object Unauthenticated : MainActivityUiState
}
