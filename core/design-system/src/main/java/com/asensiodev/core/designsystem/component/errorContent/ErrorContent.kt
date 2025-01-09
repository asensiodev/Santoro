package com.asensiodev.core.designsystem.component.errorContent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    vertical = Spacings.spacing32,
                    horizontal = Spacings.spacing16,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = Spacings.spacing16),
        )
        IconButton(onClick = onRetry) {
            Icon(
                imageVector = AppIcons.RefreshIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier =
                    Modifier
                        .size(Size.size40)
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(Spacings.spacing8),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ErrorContentPreview() {
    PreviewContent {
        ErrorContent(
            message = "Ups! An error occurred, please tap to retry",
            onRetry = {},
        )
    }
}
