package com.asensiodev.settings.impl.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size

@Composable
fun SettingsItem(
    text: String,
    painter: Painter,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = true,
) {
    SettingsItemContent(
        text = text,
        icon = {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(Size.size24),
            )
        },
        onClick = onClick,
        color = color,
        showChevron = showChevron,
    )
}

@Composable
fun SettingsItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = true,
) {
    SettingsItemContent(
        text = text,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(Size.size24),
            )
        },
        onClick = onClick,
        color = color,
        showChevron = showChevron,
    )
}

@Composable
private fun SettingsItemContent(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    color: Color,
    showChevron: Boolean,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
    ) {
        Column {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Size.size16, vertical = Size.size16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Size.size16),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    icon()
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = color,
                    )
                }

                if (showChevron) {
                    Icon(
                        imageVector = AppIcons.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
    }
}
