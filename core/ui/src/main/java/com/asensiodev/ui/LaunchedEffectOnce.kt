package com.asensiodev.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun LaunchedEffectOnce(block: () -> Unit) {
    var hasRun by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasRun) {
            hasRun = true
            block()
        }
    }
}
