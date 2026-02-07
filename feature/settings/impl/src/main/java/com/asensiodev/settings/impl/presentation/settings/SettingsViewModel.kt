package com.asensiodev.settings.impl.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel
    @Inject
    constructor(
        private val observeAuthStateUseCase: ObserveAuthStateUseCase,
        private val signOutUseCase: SignOutUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        fun observeAuthState() {
            viewModelScope.launch {
                observeAuthStateUseCase().collect { user ->
                    _uiState.update {
                        it.copy(
                            isAnonymous = user?.isAnonymous == true,
                        )
                    }
                }
            }
        }

        fun onLogoutClicked() {
            viewModelScope.launch {
                signOutUseCase()
            }
        }
    }
