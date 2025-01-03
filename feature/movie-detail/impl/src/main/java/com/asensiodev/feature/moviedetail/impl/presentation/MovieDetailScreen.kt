package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.moviedetail.impl.presentation.component.AnimatedIconWithText
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
        onRetry = { viewModel.fetchMovieDetails(uiState.movie?.id ?: 0) },
        onToggleWatchlist = {
            viewModel.toggleWatchlist()
        },
        onToggleWatched = {
            viewModel.toggleWatched()
        },
        modifier = modifier,
    )
}

@Composable
internal fun MovieDetailScreen(
    uiState: MovieDetailUiState,
    onToggleWatchlist: () -> Unit,
    onToggleWatched: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.errorMessage != null ->
            ErrorContent(
                message = uiState.errorMessage,
                onRetry = { onRetry() },
            )

        uiState.movie != null ->
            MovieDetailContent(
                uiState = uiState,
                onToggleWatchlist = onToggleWatchlist,
                onToggleWatched = onToggleWatched,
                modifier = modifier,
            )
    }
}

@Composable
internal fun MovieDetailContent(
    uiState: MovieDetailUiState,
    onToggleWatchlist: () -> Unit,
    onToggleWatched: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacings.spacing16)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        AsyncImage(
            model = uiState.movie?.posterPath,
            contentDescription = uiState.movie?.title ?: stringResource(SR.string.untitled_movie),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .aspectRatio(POSTER_ASPECT_RATIO),
            contentScale = ContentScale.Crop,
            error = painterResource(DR.drawable.ic_movie_card_placeholder),
        )
        Text(
            text = uiState.movie?.title ?: stringResource(SR.string.untitled_movie),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AdditionalMovieInfo(
                releaseDate = uiState.movie?.releaseDate,
                genres = uiState.movie?.genres,
                voteAverage = uiState.movie?.voteAverage,
            )
            Spacer(modifier = Modifier.weight(1f))
            UserListOptions(uiState, onToggleWatched, onToggleWatchlist)
        }
        Text(
            text = uiState.movie?.overview ?: stringResource(SR.string.default_no_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
        )
        Spacer(modifier = Modifier.height(Spacings.spacing16))
    }
}

@Composable
private fun RowScope.UserListOptions(
    uiState: MovieDetailUiState,
    onToggleWatched: () -> Unit,
    onToggleWatchlist: () -> Unit,
) {
    AnimatedIconWithText(
        isSelected = uiState.isWatched,
        onClick = onToggleWatched,
        selectedIcon = AppIcons.WatchedMoviesIcon,
        unselectedIcon = AppIcons.AddIcon,
        label = stringResource(SR.string.watched_icon_button),
        modifier = Modifier.Companion.weight(1f),
    )
    AnimatedIconWithText(
        isSelected = uiState.isInWatchlist,
        onClick = onToggleWatchlist,
        selectedIcon = AppIcons.WatchlistIcon,
        unselectedIcon = AppIcons.AddIcon,
        label = stringResource(SR.string.watchlist_icon_button),
        modifier = Modifier.Companion.weight(1f),
    )
}

@Composable
private fun AdditionalMovieInfo(
    releaseDate: String?,
    genres: List<String>?,
    voteAverage: Double?,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        modifier = modifier,
    ) {
        if (!releaseDate.isNullOrEmpty()) {
            Text(
                text = stringResource(SR.string.release_date, releaseDate),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (!genres.isNullOrEmpty()) {
            Text(
                text = stringResource(SR.string.genres, genres.joinToString(SEPARATOR)),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (voteAverage != null) {
            Text(
                text = stringResource(SR.string.rating, voteAverage),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun MovieDetailScreenPreview() {
    PreviewContent {
        MovieDetailScreen(
            uiState =
                MovieDetailUiState(
                    isLoading = false,
                    movie =
                        MovieUi(
                            id = MOVIE_ID,
                            title = "Movie Title",
                            overview = LoremIpsum(WORDS).values.first(),
                            posterPath = null,
                            releaseDate = "2023-01-01",
                            popularity = 10.0,
                            voteAverage = 8.5,
                            voteCount = 1000,
                            genres = listOf("Action", "Adventure"),
                            productionCountries = listOf("USA", "Canada"),
                        ),
                    errorMessage = null,
                    hasResults = false,
                ),
            onToggleWatchlist = {},
            onToggleWatched = {},
            onRetry = {},
        )
    }
}

private const val POSTER_ASPECT_RATIO = 2f / 3f
private const val SEPARATOR = ", "
private const val MOVIE_ID = 12
private const val WORDS = 40
