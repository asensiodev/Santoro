package com.asensiodev.login.impl.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.usecase.SignInAnonymouslyUseCase
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
internal class LoginViewModel
    @Inject
    constructor(
        private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
        private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase,
        private val googleSignInHelper: GoogleSignInHelper,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoginUiState())
        val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

        fun onSignInWithGoogleClicked(activityContext: Context) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                googleSignInHelper
                    .signIn(activityContext)
                    .onSuccess { idToken ->
                        performGoogleLogin(idToken)
                    }.onFailure { _ ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage =
                                    UiText.StringResource(
                                        SR.string.login_error_google_sign_in,
                                    ),
                            )
                        }
                    }
            }
        }

        private suspend fun performGoogleLogin(idToken: String) {
            signInWithGoogleUseCase(idToken)
                .onSuccess {
                    _uiState.update { it.copy(isSignInSuccessful = true) }
                }.onFailure { _ ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                UiText.StringResource(
                                    SR.string.login_error_google_sign_in,
                                ),
                        )
                    }
                }
        }

        fun signInAnonymously() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                signInAnonymouslyUseCase()
                    .onSuccess {
                        _uiState.update { it.copy(isSignInSuccessful = true) }
                    }.onFailure { _ ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage =
                                    UiText.StringResource(
                                        SR.string.login_error_anonymous,
                                    ),
                            )
                        }
                    }
            }
        }
    }
