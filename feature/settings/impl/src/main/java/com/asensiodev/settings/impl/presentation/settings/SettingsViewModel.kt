package com.asensiodev.settings.impl.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignOutUseCase
import com.asensiodev.core.domain.model.AppLanguage
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetThemeUseCase
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import com.asensiodev.settings.impl.domain.usecase.DeleteAccountUseCase
import com.asensiodev.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.asensiodev.santoro.core.stringresources.R as SR

@HiltViewModel
internal class SettingsViewModel
    @Inject
    constructor(
        private val observeAuthStateUseCase: ObserveAuthStateUseCase,
        private val signOutUseCase: SignOutUseCase,
        private val deleteAccountUseCase: DeleteAccountUseCase,
        private val observeThemeUseCase: ObserveThemeUseCase,
        private val setThemeUseCase: SetThemeUseCase,
        private val databaseRepository: DatabaseRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
        val effect = _effect.receiveAsFlow()

        fun process(intent: SettingsIntent) {
            when (intent) {
                is SettingsIntent.ObserveAuth -> observeAuth()
                is SettingsIntent.ObserveTheme -> observeTheme()
                is SettingsIntent.OnAppearanceClicked -> onAppearanceClicked()
                is SettingsIntent.SetTheme -> setTheme(intent.option)
                is SettingsIntent.DismissThemePicker -> dismissThemePicker()
                is SettingsIntent.OnLanguageClicked -> onLanguageClicked()
                is SettingsIntent.SetLanguage -> setLanguage(intent.language)
                is SettingsIntent.DismissLanguagePicker -> dismissLanguagePicker()
                is SettingsIntent.OnLogoutClicked -> onLogoutClicked()
                is SettingsIntent.OnDeleteAccountClicked -> onDeleteAccountClicked()
                is SettingsIntent.ConfirmDeleteAccount -> confirmDeleteAccount()
                is SettingsIntent.DismissDeleteAccountDialog -> dismissDeleteAccountDialog()
            }
        }

        private fun observeAuth() {
            viewModelScope.launch {
                observeAuthStateUseCase().collect { user ->
                    _uiState.update { it.copy(isAnonymous = user?.isAnonymous == true) }
                }
            }
        }

        private fun observeTheme() {
            viewModelScope.launch {
                observeThemeUseCase().collect { theme ->
                    _uiState.update { it.copy(currentTheme = theme) }
                }
            }
        }

        private fun onAppearanceClicked() {
            _uiState.update { it.copy(showThemePicker = true) }
        }

        private fun dismissThemePicker() {
            _uiState.update { it.copy(showThemePicker = false) }
        }

        private fun setTheme(option: ThemeOption) {
            viewModelScope.launch {
                setThemeUseCase(option)
                _uiState.update { it.copy(showThemePicker = false) }
            }
        }

        private fun onLanguageClicked() {
            val locales = AppCompatDelegate.getApplicationLocales()
            val currentTag = if (locales.isEmpty) AppLanguage.ENGLISH.tag else locales[0]?.language
            val current =
                AppLanguage.entries.firstOrNull { it.tag == currentTag } ?: AppLanguage.ENGLISH
            _uiState.update { it.copy(showLanguagePicker = true, currentLanguage = current) }
        }

        private fun setLanguage(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
            _uiState.update { it.copy(showLanguagePicker = false, currentLanguage = language) }
        }

        private fun dismissLanguagePicker() {
            _uiState.update { it.copy(showLanguagePicker = false) }
        }

        private fun onLogoutClicked() {
            viewModelScope.launch {
                databaseRepository.clearAllUserData()
                signOutUseCase()
            }
        }

        private fun onDeleteAccountClicked() {
            _uiState.update { it.copy(showDeleteAccountDialog = true) }
        }

        private fun dismissDeleteAccountDialog() {
            _uiState.update { it.copy(showDeleteAccountDialog = false) }
        }

        private fun confirmDeleteAccount() {
            _uiState.update { it.copy(showDeleteAccountDialog = false, isLoading = true) }
            viewModelScope.launch {
                deleteAccountUseCase()
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                    }.onFailure {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error =
                                    UiText.StringResource(
                                        SR.string.settings_delete_account_error,
                                    ),
                            )
                        }
                    }
            }
        }
    }
