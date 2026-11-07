package com.asensiodev.core.designsystem.component.statusribbon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
fun MovieStatusRibbon(
    status: MovieRibbonStatus,
    modifier: Modifier = Modifier,
    tagSize: Dp = Size.size56,
    iconPadding: Dp = Spacings.spacing8,
    announceStatus: Boolean = true,
) {
    val presentation = status.presentation()

    Box(
        modifier =
            modifier
                .size(tagSize)
                .drawBehind {
                    val path =
                        Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width, size.height)
                            close()
                        }
                    drawPath(path = path, color = presentation.containerColor)
                },
    ) {
        Icon(
            imageVector = presentation.icon,
            contentDescription = presentation.contentDescription.takeIf { announceStatus },
            tint = presentation.contentColor,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(iconPadding)
                    .size(Size.size18),
        )
    }
}

@Composable
fun MovieStatusLabel(
    status: MovieRibbonStatus,
    modifier: Modifier = Modifier,
) {
    val presentation = status.presentation()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = presentation.icon,
            contentDescription = null,
            tint = presentation.accentColor,
            modifier = Modifier.size(Size.size16),
        )
        Text(
            text = presentation.contentDescription,
            style = MaterialTheme.typography.labelMedium,
            color = presentation.accentColor,
        )
    }
}

@Composable
private fun MovieRibbonStatus.presentation(): MovieStatusPresentation =
    when (this) {
        MovieRibbonStatus.WATCHED ->
            MovieStatusPresentation(
                icon = AppIcons.Watched,
                contentDescription = stringResource(SR.string.watched_icon_button_marked),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                accentColor = MaterialTheme.colorScheme.secondary,
            )
        MovieRibbonStatus.WATCHLIST ->
            MovieStatusPresentation(
                icon = AppIcons.Watchlist,
                contentDescription = stringResource(SR.string.watchlist_icon_button_added),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                accentColor = MaterialTheme.colorScheme.tertiary,
            )
    }

private data class MovieStatusPresentation(
    val icon: ImageVector,
    val contentDescription: String,
    val containerColor: Color,
    val contentColor: Color,
    val accentColor: Color,
)

@PreviewLightDark
@Composable
private fun MovieStatusRibbonPreview() {
    PreviewContent {
        Row {
            MovieStatusRibbon(MovieRibbonStatus.WATCHED)
            MovieStatusRibbon(MovieRibbonStatus.WATCHLIST)
        }
    }
}

@PreviewLightDark
@Composable
private fun MovieStatusLabelPreview() {
    PreviewContent {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacings.spacing16)) {
            MovieStatusLabel(MovieRibbonStatus.WATCHED)
            MovieStatusLabel(MovieRibbonStatus.WATCHLIST)
        }
    }
}
