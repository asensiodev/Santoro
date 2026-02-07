package com.asensiodev.settings.impl.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.exception.AccountCollisionException
import com.asensiodev.auth.domain.usecase.LinkWithGoogleUseCase
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignInWithGoogleUseCase
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
internal class ProfileViewModel
    @Inject
    constructor(
        private val observeAuthStateUseCase: ObserveAuthStateUseCase,
        private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
        private val linkWithGoogleUseCase: LinkWithGoogleUseCase,
        private val googleSignInHelper: GoogleSignInHelper,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProfileUiState())
        val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

        private var pendingIdToken: String? = null

        fun observeAuthState() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                observeAuthStateUseCase().collect { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            isAnonymous = user?.isAnonymous == true,
                        )
                    }
                }
            }
        }

        fun onSignInWithGoogleClicked(context: Context) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
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
                                error = null,
                            )
                        }
                    }.onFailure { error ->
                        if (error is AccountCollisionException) {
                            pendingIdToken = idToken
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    showAccountCollisionDialog = true,
                                    error = null,
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
                        _uiState.update { it.copy(isLoading = false, error = null) }
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

        fun onLinkAccountSuccessDismiss() {
            _uiState.update { it.copy(isLinkAccountSuccessful = false) }
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
    }
