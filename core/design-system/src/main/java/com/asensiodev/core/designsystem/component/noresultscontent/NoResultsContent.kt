package com.asensiodev.core.designsystem.component.noresultscontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings

@Composable
fun NoResultsContent(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = AppIcons.SearchOff,
    subtitle: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacings.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing24, Alignment.CenterVertically),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Size.size80),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ICON_ALPHA),
            )

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val ICON_ALPHA = 0.5f

@PreviewLightDark
@Composable
private fun NoResultsContentPreview() {
    PreviewContent {
        NoResultsContent(
            text = "Ups! No results found...",
        )
    }
}

@PreviewLightDark
@Composable
private fun NoResultsContentWithSubtitlePreview() {
    PreviewContent {
        NoResultsContent(
            text = "No movies yet",
            subtitle = "Search for movies to add them here",
            icon = AppIcons.SearchOff,
        )
    }
}
