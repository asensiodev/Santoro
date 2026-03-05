package com.asensiodev.settings.impl.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.asensiodev.settings.api.navigation.ProfileRoute
import com.asensiodev.settings.api.navigation.SettingsRoute
import com.asensiodev.settings.impl.presentation.profile.ProfileScreenRoute
import com.asensiodev.settings.impl.presentation.settings.SettingsScreenRoute

private typealias EnterAnim =
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?

private typealias ExitAnim =
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?

fun NavGraphBuilder.profileRoute(onAppSettingsClicked: () -> Unit) {
    composable<ProfileRoute> {
        ProfileScreenRoute(
            onAppSettingsClicked = onAppSettingsClicked,
        )
    }
}

fun NavGraphBuilder.settingsRoute(
    onBackClicked: () -> Unit,
    enterTransition: EnterAnim? = null,
    exitTransition: ExitAnim? = null,
    popEnterTransition: EnterAnim? = null,
    popExitTransition: ExitAnim? = null,
) {
    composable<SettingsRoute>(
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
    ) {
        SettingsScreenRoute(onBackClicked = onBackClicked)
    }
}
