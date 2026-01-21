package com.asensiodev.login.impl.presentation

import com.asensiodev.ui.UiText

internal data class LoginUiState(
    val isLoading: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val errorMessage: UiText? = null,
)
