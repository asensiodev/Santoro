package com.asensiodev.core.designsystem.component.banner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
fun Banner(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    retryTextResource: Int = SR.string.error_message_retry_button,
    onRetry: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .border(
                    border = BorderStroke(Size.size1, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(Size.size12),
                ).background(
                    MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(Size.size12),
                ).padding(Size.size16),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
        Spacer(modifier = Modifier.width(Size.size8))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(Size.size8))
                Text(
                    text = stringResource(id = retryTextResource),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.Underline,
                        ),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier =
                        Modifier
                            .align(Alignment.End)
                            .clickable(onClick = onRetry),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun BannerPreview() {
    SantoroTheme {
        Banner(
            message = "Ups! An error occurred. Please try again later.",
            onRetry = {},
        )
    }
}
