package com.asensiodev.settings.impl.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignInWithGoogleUseCase
import com.asensiodev.auth.domain.usecase.SignOutUseCase
import com.asensiodev.auth.helper.GoogleSignInHelper
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
        private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
        private val googleSignInHelper: GoogleSignInHelper,
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

        fun onSignInWithGoogleClicked(context: Context) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                googleSignInHelper
                    .signIn(context)
                    .onSuccess { idToken ->
                        signInWithGoogleUseCase(idToken)
                            .onSuccess {
                                _uiState.update { it.copy(isLoading = false) }
                            }.onFailure {
                                _uiState.update { it.copy(isLoading = false) }
                            }
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                    }
            }
        }

        fun onLogoutClicked() {
            viewModelScope.launch {
                signOutUseCase()
            }
        }
    }
