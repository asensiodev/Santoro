package com.asensiodev.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.asensiodev.core.designsystem.theme.SantoroTheme

@Composable
fun PreviewContent(content: @Composable () -> Unit) {
    SantoroTheme {
        Box(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background),
        ) {
            content()
        }
    }
}
