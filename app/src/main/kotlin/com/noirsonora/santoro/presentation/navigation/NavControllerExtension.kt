package com.noirsonora.santoro.presentation.navigation

import androidx.navigation.NavController
import com.noirsonora.core.util.UiEvent

fun NavController.navigate(event: UiEvent.Navigate) {
    this.navigate(event.route)
}