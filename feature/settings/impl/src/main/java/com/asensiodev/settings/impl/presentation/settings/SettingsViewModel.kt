package com.asensiodev.settings.impl.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignOutUseCase
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel
    @Inject
    constructor(
        private val observeAuthStateUseCase: ObserveAuthStateUseCase,
        private val signOutUseCase: SignOutUseCase,
        private val observeThemeUseCase: ObserveThemeUseCase,
        private val setThemeUseCase: SetThemeUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
        val effect = _effect.receiveAsFlow()

        fun process(intent: SettingsIntent) {
            when (intent) {
                is SettingsIntent.ObserveAuth -> observeAuthState()
                is SettingsIntent.OnAppearanceClicked -> onAppearanceClicked()
                is SettingsIntent.SetTheme -> setTheme(intent.option)
                is SettingsIntent.DismissThemePicker -> dismissThemePicker()
                is SettingsIntent.OnLogoutClicked -> onLogoutClicked()
            }
        }

        fun observeAuthState() {
            viewModelScope.launch {
                observeAuthStateUseCase().collect { user ->
                    _uiState.update { it.copy(isAnonymous = user?.isAnonymous == true) }
                }
            }
            viewModelScope.launch {
                observeThemeUseCase().collect { theme ->
                    _uiState.update { it.copy(currentTheme = theme) }
                }
            }
        }

        fun onAppearanceClicked() {
            _uiState.update { it.copy(showThemePicker = true) }
        }

        fun dismissThemePicker() {
            _uiState.update { it.copy(showThemePicker = false) }
        }

        fun setTheme(option: ThemeOption) {
            viewModelScope.launch {
                setThemeUseCase(option)
                _uiState.update { it.copy(showThemePicker = false) }
            }
        }

        fun onLogoutClicked() {
            viewModelScope.launch {
                signOutUseCase()
            }
        }
    }
