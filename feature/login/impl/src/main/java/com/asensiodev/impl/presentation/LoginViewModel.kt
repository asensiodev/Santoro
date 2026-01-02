package com.asensiodev.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.impl.domain.usecase.SignInAnonymouslyUseCase
import com.asensiodev.impl.domain.usecase.SignInWithCredentialUseCase
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LoginViewModel
    @Inject
    constructor(
        private val signInWithCredentialUseCase: SignInWithCredentialUseCase,
        private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoginUiState())
        val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

        fun signInWithCredential(credential: AuthCredential) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                signInWithCredentialUseCase(credential)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false, isSignInSuccessful = true) }
                    }.onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                    }
            }
        }

        fun signInAnonymously() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                signInAnonymouslyUseCase()
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false, isSignInSuccessful = true) }
                    }.onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                    }
            }
        }

        fun resetState() {
            _uiState.update { LoginUiState() }
        }
    }
