package com.asensiodev.feature.searchmovies.impl.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.designsystem.R as DR

@Composable
internal fun MovieCard(
    movie: MovieUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (movie.posterPath != null) {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(movie.posterPath)
                            .crossfade(true)
                            .build(),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(Spacings.spacing8),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(DR.drawable.ic_movie_card_placeholder),
                        contentDescription = null,
                        modifier = Modifier.size(Size.size48),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = Spacings.spacing8),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MovieCardPreview() {
    val sampleMovie =
        MovieUi(
            id = 1,
            title = "Sample Movie",
            posterPath = null,
            backdropPath = null,
            voteAverage = 10.0,
        )

    PreviewContentFullSize {
        Box(modifier = Modifier.padding(16.dp)) {
            MovieCard(
                movie = sampleMovie,
                onClick = {},
                modifier = Modifier.size(width = 120.dp, height = 180.dp),
            )
        }
    }
}
