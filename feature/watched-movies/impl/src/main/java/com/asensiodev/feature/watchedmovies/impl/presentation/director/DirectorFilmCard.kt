package com.asensiodev.feature.watchedmovies.impl.presentation.director

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.component.statusribbon.MovieRibbonStatus
import com.asensiodev.core.designsystem.component.statusribbon.MovieStatusRibbon
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm
import com.asensiodev.feature.watchedmovies.impl.presentation.component.PlaceholderContent
import com.asensiodev.feature.watchedmovies.impl.presentation.mapper.BASE_POSTER_URL
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun DirectorFilmCard(
    film: DirectorFilm,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state =
        if (film.isWatched) {
            stringResource(SR.string.director_collection_watched)
        } else {
            stringResource(SR.string.director_collection_not_watched)
        }
    Card(
        onClick = onClick,
        modifier = modifier.semantics { stateDescription = state },
        shape = MaterialTheme.shapes.medium,
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (film.isWatched) Size.size2 else Size.size0,
            ),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .alpha(if (film.isWatched) FULL_ALPHA else DIMMED_ALPHA),
            ) {
                PlaceholderContent(title = film.title)
                if (film.posterPath != null) {
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(BASE_POSTER_URL + film.posterPath)
                                .crossfade(true)
                                .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        colorFilter = if (film.isWatched) null else DesaturatedFilter,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            film.ribbonStatus()?.let { status ->
                MovieStatusRibbon(
                    status = status,
                    modifier = Modifier.align(Alignment.TopEnd),
                    announceStatus = status == MovieRibbonStatus.WATCHLIST,
                )
            }
        }
    }
}

private fun DirectorFilm.ribbonStatus(): MovieRibbonStatus? =
    when {
        isWatched -> MovieRibbonStatus.WATCHED
        isInWatchlist -> MovieRibbonStatus.WATCHLIST
        else -> null
    }

private val DesaturatedFilter =
    ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(PENDING_SATURATION) })
private const val FULL_ALPHA = 1f
private const val DIMMED_ALPHA = 0.6f
private const val PENDING_SATURATION = 0.25f
private const val PREVIEW_POSTER_RATIO = 2f / 3f

@PreviewLightDark
@Composable
private fun DirectorFilmCardPreview() {
    PreviewContent {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
            listOf(true, false).forEachIndexed { index, isWatched ->
                DirectorFilmCard(
                    film = DirectorFilm(index, "Inception", null, null, isWatched = isWatched),
                    onClick = {},
                    modifier = Modifier.width(Size.size100).aspectRatio(PREVIEW_POSTER_RATIO),
                )
            }
        }
    }
}
