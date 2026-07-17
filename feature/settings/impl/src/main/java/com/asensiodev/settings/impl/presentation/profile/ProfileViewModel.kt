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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
        private var isObservingAuth = false
        private var accountActionJob: Job? = null

        fun process(intent: ProfileIntent) {
            when (intent) {
                is ProfileIntent.ObserveAuth -> observeAuthState()
                is ProfileIntent.OnLinkGoogleClicked -> onSignInWithGoogleClicked(intent.context)
                is ProfileIntent.DismissLinkSuccess -> onLinkAccountSuccessDismiss()
                is ProfileIntent.DismissAccountCollision -> onAccountCollisionDialogDismiss()
                is ProfileIntent.ConfirmAccountCollision -> onAccountCollisionDialogConfirm()
            }
        }

        private fun observeAuthState() {
            if (isObservingAuth) return
            isObservingAuth = true
            _uiState.update { it.copy(isLoading = true) }
            viewModelScope.launch {
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

        private fun onSignInWithGoogleClicked(context: Context) {
            if (accountActionJob?.isActive == true) return
            _uiState.update { it.copy(isLoading = true) }
            accountActionJob =
                viewModelScope.launch {
                    try {
                        googleSignInHelper
                            .signIn(context)
                            .onSuccess { idToken ->
                                handleGoogleSignIn(idToken)
                            }.onFailure {
                                setGoogleSignInError()
                            }
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Exception) {
                        setGoogleSignInError()
                    } finally {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }

        private fun setGoogleSignInError() {
            _uiState.update {
                it.copy(
                    error =
                        UiText.StringResource(
                            SR.string.settings_error_google_sign_in,
                        ),
                )
            }
        }

        private suspend fun handleGoogleSignIn(idToken: String) {
            if (_uiState.value.isAnonymous) {
                linkWithGoogleUseCase(idToken)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLinkAccountSuccessful = true,
                                error = null,
                            )
                        }
                    }.onFailure { error ->
                        if (error is AccountCollisionException) {
                            pendingIdToken = idToken
                            _uiState.update {
                                it.copy(
                                    showAccountCollisionDialog = true,
                                    error = null,
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
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
                        _uiState.update { it.copy(error = null) }
                    }.onFailure {
                        setGoogleSignInError()
                    }
            }
        }

        private fun onLinkAccountSuccessDismiss() {
            _uiState.update { it.copy(isLinkAccountSuccessful = false) }
        }

        private fun onAccountCollisionDialogDismiss() {
            pendingIdToken = null
            _uiState.update { it.copy(showAccountCollisionDialog = false) }
        }

        private fun onAccountCollisionDialogConfirm() {
            val token = pendingIdToken
            if (token != null && accountActionJob?.isActive != true) {
                pendingIdToken = null
                _uiState.update {
                    it.copy(
                        showAccountCollisionDialog = false,
                        isLoading = true,
                    )
                }
                accountActionJob =
                    viewModelScope.launch {
                        try {
                            signInWithGoogleUseCase(token)
                                .onSuccess {
                                    _uiState.update { it.copy(error = null) }
                                }.onFailure {
                                    setGoogleSignInError()
                                }
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (_: Exception) {
                            setGoogleSignInError()
                        } finally {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
            }
        }
    }
