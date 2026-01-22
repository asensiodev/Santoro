package com.asensiodev.feature.searchmovies.impl.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun HeroMovieCard(
    movie: MovieUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = movie.backdropPath ?: movie.posterPath

            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize(),
            )

            GradientOverlay()

            HeroCardContent(
                title = movie.title,
                voteAverage = movie.voteAverage,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(Spacings.spacing16)
                        .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun GradientOverlay() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = GRADIENT_ALPHA),
                                Color.Black,
                            ),
                        startY = 0f,
                    ),
                ),
    )
}

private const val AMBER_COLOR = 0xFFFFC107

@Composable
private fun HeroCardContent(
    title: String,
    voteAverage: Double,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(Spacings.spacing8))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = Color(AMBER_COLOR),
                modifier = Modifier.size(Size.size18),
            )
            Spacer(modifier = Modifier.width(Spacings.spacing4))
            Text(
                text = stringResource(SR.string.vote_average_format, voteAverage),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = TEXT_ALPHA),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private const val GRADIENT_ALPHA = 0.6f
private const val TEXT_ALPHA = 0.9f

@PreviewLightDark
@Composable
private fun HeroMovieCardPreview() {
    SantoroTheme {
        HeroMovieCard(
            movie =
                MovieUi(
                    id = 1,
                    title = "The Lord of the Rings: The Fellowship of the Ring",
                    posterPath = null,
                    backdropPath = null,
                    voteAverage = 8.8,
                ),
            onClick = {},
            modifier = Modifier.height(Size.size200).fillMaxWidth(),
        )
    }
}
