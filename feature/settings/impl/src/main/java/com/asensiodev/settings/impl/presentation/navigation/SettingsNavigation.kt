package com.asensiodev.settings.impl.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.asensiodev.settings.api.navigation.SettingsRoute
import com.asensiodev.settings.impl.presentation.SettingsScreenRoute

fun NavGraphBuilder.settingsRoute(onBackClicked: () -> Unit) {
    composable<SettingsRoute> {
        SettingsScreenRoute(onBackClicked = onBackClicked)
    }
}
