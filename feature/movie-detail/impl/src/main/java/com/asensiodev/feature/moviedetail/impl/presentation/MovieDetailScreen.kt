package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi
import javax.inject.Inject
import com.asensiodev.santoro.core.designsystem.R as DR
import com.asensiodev.santoro.core.stringresources.R as SR

class MovieDetailScreen
    @Inject
    constructor() {
        @Composable
        fun Screen(movieId: Int) {
            MovieDetailRoot(
                movieId = movieId,
            )
        }
    }

@Composable
internal fun MovieDetailRoot(
    movieId: Int,
    modifier: Modifier = Modifier,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(movieId) {
        viewModel.fetchMovieDetails(movieId)
    }

    MovieDetailScreen(
        uiState = uiState,
        modifier = modifier,
    )
}

@Composable
internal fun MovieDetailScreen(
    uiState: MovieDetailUiState,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.errorMessage != null ->
            ErrorContent(
                message = uiState.errorMessage,
                onRetry = { /* Retry logic */ },
            )

        uiState.movie != null ->
            MovieDetailContent(
                movie = uiState.movie,
                modifier = modifier,
            )
    }
}

private const val POSTER_ASPECT_RATIO = 2f / 3f

@Composable
internal fun MovieDetailContent(
    movie: MovieUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacings.spacing16),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        AsyncImage(
            model = movie.posterPath,
            contentDescription = movie.title,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .aspectRatio(POSTER_ASPECT_RATIO),
            contentScale = ContentScale.Crop,
            error = painterResource(DR.drawable.ic_movie_card_placeholder),
        )
        Text(
            text = movie.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = movie.overview.ifEmpty { stringResource(SR.string.default_no_description) },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
        )
        Spacer(modifier = Modifier.height(Spacings.spacing16))
        AdditionalMovieInfo(
            releaseDate = movie.releaseDate,
            genres = movie.genres,
            voteAverage = movie.voteAverage,
            voteCount = movie.voteCount,
        )
    }
}

@Composable
private fun AdditionalMovieInfo(
    releaseDate: String?,
    genres: List<String>,
    voteAverage: Double,
    voteCount: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        if (!releaseDate.isNullOrEmpty()) {
            Text(
                text = stringResource(SR.string.release_date, releaseDate),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (genres.isNotEmpty()) {
            Text(
                text = stringResource(SR.string.genres, genres.joinToString(", ")),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            text = stringResource(SR.string.rating, voteAverage, voteCount),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
