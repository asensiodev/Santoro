package com.noirsonora.santoro.navigation

import androidx.navigation.NavController
import com.noirsonora.core.util.UiEvent

fun NavController.navigate(event: UiEvent.Navigate) {
    this.navigate(event.route)
}

fun NavController.navigateAndPopBackstack(event: UiEvent.Navigate) {
    this.popBackStack()
    this.navigate(event.route)
}