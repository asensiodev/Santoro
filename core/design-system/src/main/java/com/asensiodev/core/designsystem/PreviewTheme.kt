package com.asensiodev.core.designsystem

import androidx.compose.runtime.Composable
import com.asensiodev.core.designsystem.theme.SantoroTheme

@Composable
fun PreviewContent(content: @Composable () -> Unit) {
    SantoroTheme {
        content()
    }
}
