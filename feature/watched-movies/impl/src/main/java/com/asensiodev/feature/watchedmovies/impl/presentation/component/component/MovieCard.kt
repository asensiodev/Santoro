package com.asensiodev.feature.watchedmovies.impl.presentation.component.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
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
internal fun MovieCard(
    movie: MovieUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(Size.size128),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            if (movie.posterPath == null) {
                Icon(
                    painter = painterResource(DR.drawable.ic_movie_card_placeholder),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(Size.size160)
                            .weight(FULL_WEIGHT)
                            .padding(Spacings.spacing8),
                )
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(Spacings.spacing8),
                )
            } else {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(movie.posterPath)
                            .crossfade(true)
                            .build(),
                    contentDescription = movie.title,
                    // placeholder = painterResource(DR.drawable.ic_movie_card_placeholder),
                    error = painterResource(DR.drawable.ic_movie_card_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .size(Size.size160)
                            .clip(MaterialTheme.shapes.medium),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MovieCardPreview() {
    PreviewContent {
        MovieCard(
            movie =
                MovieUi(
                    id = 1,
                    title = "Sample Movie",
                    posterPath = null,
                ),
            onClick = {},
        )
    }
}

private const val FULL_WEIGHT = 1f
