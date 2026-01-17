package com.asensiodev.login.impl.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.asensiodev.api.navigation.LoginRoute
import com.asensiodev.login.impl.presentation.LoginRoute as LoginScreenRoute

fun NavController.navigateToLogin(navOptions: NavOptions? = null) {
    this.navigate(LoginRoute, navOptions)
}

fun NavGraphBuilder.loginScreen() {
    composable<LoginRoute> {
        LoginScreenRoute()
    }
}
