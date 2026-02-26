package com.asensiodev.login.impl.presentation

import android.content.Context

internal sealed interface LoginIntent {
    data class SignInWithGoogle(
        val context: Context,
    ) : LoginIntent
    data object SignInAnonymously : LoginIntent
}
