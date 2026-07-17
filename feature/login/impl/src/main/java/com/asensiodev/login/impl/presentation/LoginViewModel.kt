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

        fun process(intent: LoginIntent) {
            when (intent) {
                is LoginIntent.SignInWithGoogle -> onSignInWithGoogleClicked(intent.context)
                is LoginIntent.SignInAnonymously -> signInAnonymously()
            }
        }

        private fun onSignInWithGoogleClicked(activityContext: Context) {
            if (!startSignIn()) return
            viewModelScope.launch {
                try {
                    googleSignInHelper
                        .signIn(activityContext)
                        .onSuccess { idToken -> performGoogleLogin(idToken) }
                        .onFailure { showError(SR.string.login_error_google_sign_in) }
                } finally {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }

        private suspend fun performGoogleLogin(idToken: String) {
            signInWithGoogleUseCase(idToken)
                .onSuccess {
                    _uiState.update { it.copy(isSignInSuccessful = true) }
                }.onFailure { showError(SR.string.login_error_google_sign_in) }
        }

        private fun signInAnonymously() {
            if (!startSignIn()) return
            viewModelScope.launch {
                try {
                    signInAnonymouslyUseCase()
                        .onSuccess {
                            _uiState.update { it.copy(isSignInSuccessful = true) }
                        }.onFailure { showError(SR.string.login_error_anonymous) }
                } finally {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }

        private fun startSignIn(): Boolean {
            while (true) {
                val state = _uiState.value
                if (state.isLoading || state.isSignInSuccessful) return false
                if (
                    _uiState.compareAndSet(
                        state,
                        state.copy(isLoading = true, errorMessage = null),
                    )
                ) {
                    return true
                }
            }
        }

        private fun showError(messageResource: Int) {
            _uiState.update {
                it.copy(errorMessage = UiText.StringResource(messageResource))
            }
        }
    }
