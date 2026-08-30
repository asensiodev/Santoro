package com.asensiodev.settings.impl.presentation.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignOutUseCase
import com.asensiodev.auth.helper.GoogleSignInHelper
import com.asensiodev.core.domain.model.AppLanguage
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetThemeUseCase
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import com.asensiodev.settings.impl.domain.usecase.DeleteAccountUseCase
import com.asensiodev.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
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
        private val syncRepository: SyncRepository,
        private val googleSignInHelper: GoogleSignInHelper,
    ) : ViewModel() {
        private var currentUser: SantoroUser? = null
        private var isObservingAuth = false
        private var isObservingTheme = false
        private var accountActionJob: Job? = null
        private val nextMessageId = AtomicLong(0L)

        private val _uiState =
            MutableStateFlow(SettingsUiState(currentLanguage = resolveCurrentLanguage()))
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
                is SettingsIntent.ConfirmDeleteAccount -> confirmDeleteAccount(intent.context)
                is SettingsIntent.DismissDeleteAccountDialog -> dismissDeleteAccountDialog()
                is SettingsIntent.ErrorShown -> onErrorShown(intent.messageId)
            }
        }

        private fun observeAuth() {
            if (isObservingAuth) return
            isObservingAuth = true
            viewModelScope.launch {
                observeAuthStateUseCase().collect { user ->
                    currentUser = user
                    _uiState.update {
                        it.copy(showAccountActions = user != null && !user.isAnonymous)
                    }
                }
            }
        }

        private fun observeTheme() {
            if (isObservingTheme) return
            isObservingTheme = true
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
            _uiState.update {
                it.copy(
                    showLanguagePicker = true,
                    currentLanguage = resolveCurrentLanguage(),
                )
            }
        }

        private fun setLanguage(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
            _uiState.update { it.copy(showLanguagePicker = false, currentLanguage = language) }
        }

        private fun dismissLanguagePicker() {
            _uiState.update { it.copy(showLanguagePicker = false) }
        }

        private fun onLogoutClicked() {
            if (accountActionJob?.isActive == true) return
            _uiState.update { it.copy(isLoading = true) }
            accountActionJob =
                viewModelScope.launch {
                    val user = currentUser
                    try {
                        val syncResult =
                            if (user != null && !user.isAnonymous) {
                                syncRepository.uploadPendingChanges(user.uid)
                            } else {
                                Result.success(Unit)
                            }
                        val syncError = syncResult.exceptionOrNull()
                        if (syncError == null) {
                            signOutUseCase()
                        } else {
                            showError(SR.string.settings_logout_error)
                        }
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Exception) {
                        showError(SR.string.settings_logout_error)
                    } finally {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }

        private fun onDeleteAccountClicked() {
            _uiState.update { it.copy(showDeleteAccountDialog = true) }
        }

        private fun dismissDeleteAccountDialog() {
            _uiState.update { it.copy(showDeleteAccountDialog = false) }
        }

        private fun confirmDeleteAccount(context: Context) {
            if (accountActionJob?.isActive == true) return
            _uiState.update { it.copy(showDeleteAccountDialog = false, isLoading = true) }
            accountActionJob =
                viewModelScope.launch {
                    try {
                        val idToken = googleSignInHelper.signIn(context).getOrNull()
                        val user = currentUser
                        if (idToken == null || user == null || user.isAnonymous) {
                            showError(SR.string.settings_delete_account_error)
                        } else {
                            deleteAccountUseCase(user.uid, idToken)
                                .onFailure {
                                    showError(SR.string.settings_delete_account_error)
                                }
                        }
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Exception) {
                        showError(SR.string.settings_delete_account_error)
                    } finally {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }

        private fun showError(messageRes: Int) {
            val pendingMessage =
                SettingsPendingMessage(
                    id = nextMessageId.incrementAndGet(),
                    message = UiText.StringResource(messageRes),
                )
            _uiState.update { state ->
                state.copy(pendingMessage = pendingMessage)
            }
        }

        private fun onErrorShown(messageId: Long) {
            _uiState.update { state ->
                if (state.pendingMessage?.id == messageId) {
                    state.copy(pendingMessage = null)
                } else {
                    state
                }
            }
        }

        companion object {
            fun resolveCurrentLanguage(): AppLanguage {
                val locales = AppCompatDelegate.getApplicationLocales()
                val currentTag =
                    if (locales.isEmpty) {
                        java.util.Locale
                            .getDefault()
                            .language
                    } else {
                        locales[0]?.language
                    }
                return if (currentTag == AppLanguage.SPANISH.tag) {
                    AppLanguage.SPANISH
                } else {
                    AppLanguage.ENGLISH
                }
            }
        }
    }
