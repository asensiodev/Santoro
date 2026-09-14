package com.asensiodev.settings.impl.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.exception.AccountCollisionException
import com.asensiodev.auth.domain.model.ExpectedUserSignOutOutcome
import com.asensiodev.auth.domain.usecase.LinkWithGoogleUseCase
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignOutUseCase
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
        private val linkWithGoogleUseCase: LinkWithGoogleUseCase,
        private val signOutUseCase: SignOutUseCase,
        private val googleSignInHelper: GoogleSignInHelper,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProfileUiState())
        val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

        private var collisionAnonymousUid: String? = null
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
            val expectedUid =
                _uiState.value.user
                    ?.takeIf { it.isAnonymous }
                    ?.uid ?: return
            _uiState.update { it.copy(isLoading = true) }
            accountActionJob =
                viewModelScope.launch {
                    try {
                        googleSignInHelper
                            .signIn(context)
                            .onSuccess { idToken ->
                                handleGoogleSignIn(expectedUid, idToken)
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

        private suspend fun handleGoogleSignIn(
            expectedUid: String,
            idToken: String,
        ) {
            linkWithGoogleUseCase(expectedUid, idToken)
                .onSuccess { linkedUser ->
                    if (linkedUser.uid == expectedUid) {
                        _uiState.update {
                            it.copy(
                                isLinkAccountSuccessful = true,
                                error = null,
                            )
                        }
                    } else {
                        setLinkingError()
                    }
                }.onFailure { error ->
                    if (error is AccountCollisionException) {
                        collisionAnonymousUid = expectedUid
                        _uiState.update {
                            it.copy(
                                showAccountCollisionDialog = true,
                                error = null,
                            )
                        }
                    } else {
                        setLinkingError()
                    }
                }
        }

        private fun setLinkingError() {
            _uiState.update {
                it.copy(
                    error = UiText.StringResource(SR.string.settings_error_linking_account),
                )
            }
        }

        private fun onLinkAccountSuccessDismiss() {
            _uiState.update { it.copy(isLinkAccountSuccessful = false) }
        }

        private fun onAccountCollisionDialogDismiss() {
            collisionAnonymousUid = null
            _uiState.update { it.copy(showAccountCollisionDialog = false) }
        }

        private fun onAccountCollisionDialogConfirm() {
            val expectedUid = collisionAnonymousUid
            if (expectedUid != null && accountActionJob?.isActive != true) {
                collisionAnonymousUid = null
                _uiState.update {
                    it.copy(
                        showAccountCollisionDialog = false,
                        isLoading = true,
                    )
                }
                accountActionJob =
                    viewModelScope.launch {
                        try {
                            when (signOutUseCase(expectedUid)) {
                                ExpectedUserSignOutOutcome.SignedOut,
                                ExpectedUserSignOutOutcome.NoAuthenticatedUser,
                                -> _uiState.update { it.copy(error = null) }
                                ExpectedUserSignOutOutcome.AuthenticatedUserMismatch ->
                                    setLinkingError()
                            }
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (_: Exception) {
                            setLinkingError()
                        } finally {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
            }
        }
    }
