package com.asensiodev.settings.impl.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.asensiodev.settings.api.navigation.ProfileRoute
import com.asensiodev.settings.api.navigation.SettingsRoute
import com.asensiodev.settings.impl.presentation.profile.ProfileScreenRoute
import com.asensiodev.settings.impl.presentation.settings.SettingsScreenRoute

fun NavGraphBuilder.profileRoute(onAppSettingsClicked: () -> Unit) {
    composable<ProfileRoute> {
        ProfileScreenRoute(
            onAppSettingsClicked = onAppSettingsClicked,
        )
    }
}

fun NavGraphBuilder.settingsRoute(onBackClicked: () -> Unit) {
    composable<SettingsRoute> {
        SettingsScreenRoute(onBackClicked = onBackClicked)
    }
}
