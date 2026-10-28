package com.asensiodev.feature.watchedmovies.impl.presentation.component

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.designsystem.R as DR

@Composable
internal fun WatchedMovieRow(
    movie: MovieUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
                posterPath = movie.posterPath,
                modifier =
                    Modifier
                        .width(Size.size48)
                        .aspectRatio(POSTER_RATIO)
                        .clip(MaterialTheme.shapes.small),
            )
            Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing4)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                movie.metadata()?.let { metadata ->
                    Text(
                        text = metadata,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterThumbnail(
    posterPath: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (posterPath != null) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(posterPath)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                painter = painterResource(DR.drawable.ic_movie_card_placeholder),
                contentDescription = null,
                modifier = Modifier.size(Size.size24),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun MovieUi.metadata(): String? =
    listOfNotNull(releaseYear, directors)
        .joinToString(METADATA_SEPARATOR)
        .ifBlank { null }

private const val POSTER_RATIO = 2f / 3f
private const val METADATA_SEPARATOR = " · "

@PreviewLightDark
@Composable
private fun WatchedMovieRowPreview() {
    PreviewContent {
        WatchedMovieRow(
            movie =
                MovieUi(
                    id = 1,
                    title = "Fargo",
                    posterPath = null,
                    watchedDate = "March 2026",
                    releaseYear = "1996",
                    directors = "Joel Coen, Ethan Coen",
                ),
            onClick = {},
        )
    }
}
