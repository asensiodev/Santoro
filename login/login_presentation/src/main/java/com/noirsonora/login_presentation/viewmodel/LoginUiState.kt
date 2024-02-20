package com.noirsonora.login_presentation.viewmodel

data class LoginUiState(
    val loading: Boolean = false,
    val emailHasError: String? = null
)