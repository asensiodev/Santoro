package com.asensiodev.feature.searchmovies.impl.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.asensiodev.feature.searchmovies.impl.domain.model.MovieLibraryStatus
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun MovieStatusRibbon(
    status: MovieLibraryStatus,
    modifier: Modifier = Modifier,
    tagSize: Dp = Size.size56,
    iconPadding: Dp = Spacings.spacing8,
) {
    val presentation =
        when (status) {
            MovieLibraryStatus.Watched ->
                MovieStatusPresentation(
                    icon = AppIcons.Watched,
                    contentDescription = stringResource(SR.string.watched_icon_button_marked),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            MovieLibraryStatus.Watchlist ->
                MovieStatusPresentation(
                    icon = AppIcons.Watchlist,
                    contentDescription = stringResource(SR.string.watchlist_icon_button_added),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
        }

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
            contentDescription = presentation.contentDescription,
            tint = presentation.contentColor,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(iconPadding)
                    .size(Size.size18),
        )
    }
}

private data class MovieStatusPresentation(
    val icon: ImageVector,
    val contentDescription: String,
    val containerColor: Color,
    val contentColor: Color,
)

@PreviewLightDark
@Composable
private fun MovieStatusRibbonPreview() {
    PreviewContent {
        Row {
            MovieStatusRibbon(MovieLibraryStatus.Watched)
            MovieStatusRibbon(MovieLibraryStatus.Watchlist)
        }
    }
}
