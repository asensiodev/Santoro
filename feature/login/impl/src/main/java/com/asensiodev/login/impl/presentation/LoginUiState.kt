package com.asensiodev.login.impl.presentation

internal data class LoginUiState(
    val isLoading: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val errorMessage: String? = null,
)
