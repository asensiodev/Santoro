package com.asensiodev.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun LaunchEffectOnce(block: () -> Unit) {
    var isInitialized by rememberSaveable { mutableStateOf(false) }
    if (!isInitialized) {
        LaunchedEffect(block) {
            isInitialized = true
            block()
        }
    }
}
