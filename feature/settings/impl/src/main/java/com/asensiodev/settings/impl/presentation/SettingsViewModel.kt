package com.asensiodev.settings.impl.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.exception.AccountCollisionException
import com.asensiodev.auth.domain.usecase.LinkWithGoogleUseCase
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignInWithGoogleUseCase
import com.asensiodev.auth.domain.usecase.SignOutUseCase
import com.asensiodev.auth.helper.GoogleSignInHelper
import com.asensiodev.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
        private val linkWithGoogleUseCase: LinkWithGoogleUseCase,
        private val googleSignInHelper: GoogleSignInHelper,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        private var pendingIdToken: String? = null

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

        fun onSignInWithGoogleClicked(context: Context) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                googleSignInHelper
                    .signIn(context)
                    .onSuccess { idToken ->
                        handleGoogleSignIn(idToken)
                    }.onFailure { _ ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error =
                                    UiText.StringResource(
                                        SR.string.settings_error_google_sign_in,
                                    ),
                            )
                        }
                    }
            }
        }

        private suspend fun handleGoogleSignIn(idToken: String) {
            if (_uiState.value.isAnonymous) {
                linkWithGoogleUseCase(idToken)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLinkAccountSuccessful = true,
                            )
                        }
                    }.onFailure { error ->
                        if (error is AccountCollisionException) {
                            pendingIdToken = idToken
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    showAccountCollisionDialog = true,
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error =
                                        UiText.StringResource(
                                            SR.string.settings_error_linking_account,
                                        ),
                                )
                            }
                        }
                    }
            } else {
                signInWithGoogleUseCase(idToken)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                    }.onFailure { _ ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error =
                                    UiText.StringResource(
                                        SR.string.settings_error_google_sign_in,
                                    ),
                            )
                        }
                    }
            }
        }

        fun onAccountCollisionDialogDismiss() {
            pendingIdToken = null
            _uiState.update { it.copy(showAccountCollisionDialog = false) }
        }

        fun onAccountCollisionDialogConfirm() {
            val token = pendingIdToken
            if (token != null) {
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(
                            showAccountCollisionDialog = false,
                            isLoading = true,
                        )
                    }
                    signInWithGoogleUseCase(token)
                        .onSuccess {
                            _uiState.update { it.copy(isLoading = false) }
                        }.onFailure { _ ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error =
                                        UiText.StringResource(
                                            SR.string.settings_error_google_sign_in,
                                        ),
                                )
                            }
                        }
                }
            }
        }

        fun onErrorDismiss() {
            _uiState.update { it.copy(error = null) }
        }

        fun onLinkAccountSuccessDismiss() {
            _uiState.update { it.copy(isLinkAccountSuccessful = false) }
        }

        fun onLogoutClicked() {
            viewModelScope.launch {
                signOutUseCase()
            }
        }
    }
