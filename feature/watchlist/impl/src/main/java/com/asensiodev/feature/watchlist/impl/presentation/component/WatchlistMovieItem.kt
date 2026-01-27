package com.asensiodev.feature.watchlist.impl.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import java.util.Locale

private const val SINGLE_LINE = 1
private const val DOUBLE_LINE = 2
private const val GOLD_COLOR = 0xFFFFC107

@Composable
internal fun WatchlistMovieItem(
    movie: MovieUi,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(Size.size140),
        shape = RoundedCornerShape(Size.size16),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = Size.size2),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MovieImage(movie)
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = Spacings.spacing12, vertical = Spacings.spacing12),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = DOUBLE_LINE,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(Spacings.spacing4))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
                ) {
                    movie.releaseYear?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (movie.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(GOLD_COLOR),
                                modifier = Modifier.formatSize(Size.size14),
                            )
                            Spacer(modifier = Modifier.width(Size.size2))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", movie.rating),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacings.spacing4))
                movie.genres?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = SINGLE_LINE,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            DeleteMovieButton(onRemoveClick)
        }
    }
}

@Composable
private fun MovieImage(movie: MovieUi) {
    AsyncImage(
        model =
            ImageRequest
                .Builder(LocalContext.current)
                .data(movie.posterPath)
                .crossfade(true)
                .build(),
        contentDescription = movie.title,
        contentScale = ContentScale.Crop,
        modifier =
            Modifier
                .width(Size.size100)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = Size.size16, bottomStart = Size.size16))
                .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Composable
private fun DeleteMovieButton(onRemoveClick: () -> Unit) {
    IconButton(
        onClick = onRemoveClick,
        modifier = Modifier.padding(end = Spacings.spacing4),
    ) {
        Icon(
            imageVector = Icons.Rounded.Delete,
            contentDescription = "Remove from watchlist",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun Modifier.formatSize(size: Dp) = this.then(Modifier.width(size).height(size))

@PreviewLightDark
@Composable
private fun WatchlistMovieItemPreview() {
    val sampleMovie =
        MovieUi(
            id = 1,
            title = "Inception",
            posterPath = null,
            releaseYear = "2010",
            genres = "Science Fiction, Action",
            rating = 8.8,
        )

    PreviewContentFullSize {
        Column(modifier = Modifier.padding(Spacings.spacing16)) {
            WatchlistMovieItem(
                movie = sampleMovie,
                onClick = {},
                onRemoveClick = {},
            )
        }
    }
}
