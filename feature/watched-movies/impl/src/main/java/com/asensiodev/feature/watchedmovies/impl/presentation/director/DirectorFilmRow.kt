package com.asensiodev.feature.watchedmovies.impl.presentation.director

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.component.statusribbon.MovieRibbonStatus
import com.asensiodev.core.designsystem.component.statusribbon.MovieStatusLabel
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm
import com.asensiodev.feature.watchedmovies.impl.presentation.mapper.BASE_POSTER_URL
import com.asensiodev.santoro.core.designsystem.R as DR
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun DirectorFilmRow(
    film: DirectorFilm,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val notWatched = stringResource(SR.string.director_collection_not_watched)
    Card(
        onClick = onClick,
        modifier =
            modifier.fillMaxWidth().semantics {
                if (!film.isWatched) stateDescription = notWatched
            },
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Row(
            modifier = Modifier.padding(Spacings.spacing8),
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterThumbnail(
                posterPath = film.posterPath,
                isWatched = film.isWatched,
                modifier =
                    Modifier
                        .width(Size.size48)
                        .aspectRatio(POSTER_RATIO)
                        .clip(MaterialTheme.shapes.small),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacings.spacing4),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = film.title,
                    style = MaterialTheme.typography.titleSmall,
                    color =
                        if (film.isWatched) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                FilmMetadata(year = film.releaseYear(), status = film.ribbonStatus())
            }
        }
    }
}

@Composable
private fun FilmMetadata(
    year: String?,
    status: MovieRibbonStatus?,
) {
    if (year == null && status == null) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (year != null) {
            Text(
                text = year,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (year != null && status != null) {
            Text(
                text = METADATA_SEPARATOR,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
        if (status != null) {
            MovieStatusLabel(status = status)
        }
    }
}

@Composable
private fun PosterThumbnail(
    posterPath: String?,
    isWatched: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .alpha(if (isWatched) FULL_ALPHA else DIMMED_ALPHA),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(DR.drawable.ic_movie_card_placeholder),
            contentDescription = null,
            modifier = Modifier.size(Size.size24),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (posterPath != null) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(BASE_POSTER_URL + posterPath)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = if (isWatched) null else DesaturatedFilter,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun DirectorFilm.ribbonStatus(): MovieRibbonStatus? =
    when {
        isWatched -> MovieRibbonStatus.WATCHED
        isInWatchlist -> MovieRibbonStatus.WATCHLIST
        else -> null
    }

private fun DirectorFilm.releaseYear(): String? =
    releaseDate?.take(YEAR_LENGTH)?.takeIf { it.length == YEAR_LENGTH && it.all(Char::isDigit) }

private val DesaturatedFilter =
    ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(PENDING_SATURATION) })
private const val FULL_ALPHA = 1f
private const val DIMMED_ALPHA = 0.6f
private const val PENDING_SATURATION = 0.25f
private const val POSTER_RATIO = 2f / 3f
private const val YEAR_LENGTH = 4
private const val METADATA_SEPARATOR = " · "

@PreviewLightDark
@Composable
private fun DirectorFilmRowPreview() {
    PreviewContent {
        Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
            listOf(
                "Memento" to "2000-09-05",
                "Insomnia" to "2002-05-24",
                "Batman Begins" to "2005-06-15",
            ).forEachIndexed { index, (title, releaseDate) ->
                DirectorFilmRow(
                    film =
                        DirectorFilm(
                            movieId = index,
                            title = title,
                            posterPath = null,
                            releaseDate = releaseDate,
                            isWatched = index == 0,
                            isInWatchlist = index == 1,
                        ),
                    onClick = {},
                )
            }
        }
    }
}
