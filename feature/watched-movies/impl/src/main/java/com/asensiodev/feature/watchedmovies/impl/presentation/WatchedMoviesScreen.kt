package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.noresultscontent.NoResultsContent
import com.asensiodev.core.designsystem.component.querytextfield.QueryTextField
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchedmovies.impl.domain.model.WatchedStats
import com.asensiodev.feature.watchedmovies.impl.presentation.component.MovieCard
import com.asensiodev.feature.watchedmovies.impl.presentation.component.WatchedStatsDashboard
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun WatchedMoviesRoute(
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchedMoviesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.process(WatchedMoviesIntent.LoadMovies)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WatchedMoviesEffect.NavigateToDetail -> onMovieClick(effect.movieId)
            }
        }
    }

    val onProcess = remember(viewModel) { viewModel::process }

    WatchedMoviesScreen(
        uiState = uiState,
        onProcess = onProcess,
        onMovieClick = onMovieClick,
        modifier = modifier,
    )
}

@Composable
internal fun WatchedMoviesScreen(
    uiState: WatchedMoviesUiState,
    onProcess: (WatchedMoviesIntent) -> Unit,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = Spacings.spacing16)
                .padding(top = Spacings.spacing16),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        QueryTextField(
            query = uiState.query,
            placeholder = stringResource(SR.string.watched_movies_textfield_placeholder),
            onQueryChanged = { onProcess(WatchedMoviesIntent.UpdateQuery(it)) },
        )
        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }

            uiState.errorMessage != null -> {
                ErrorContent(
                    message = stringResource(SR.string.error_message_retry),
                    onRetry = { onProcess(WatchedMoviesIntent.LoadMovies) },
                )
            }

            uiState.hasResults -> {
                WatchedMovieList(
                    movies = uiState.movies,
                    stats = uiState.stats,
                    onMovieClick = onMovieClick,
                )
            }

            else -> {
                NoResultsContent(
                    text = stringResource(SR.string.watched_movies_no_results_text),
                )
            }
        }
    }
}

@Composable
internal fun WatchedMovieList(
    movies: Map<String, List<MovieUi>>,
    stats: WatchedStats?,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = Size.size100),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        modifier = modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            if (stats != null) {
                WatchedStatsDashboard(stats = stats)
            }
        }

        movies.forEach { (dateHeader, movieList) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                StickyHeader(title = dateHeader)
            }
            items(items = movieList, key = { it.id }) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                    modifier = Modifier.aspectRatio(POSTER_RATIO),
                )
            }
        }
    }
}

@Composable
private fun StickyHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = Spacings.spacing4),
    )
}

private const val NUMBER_OF_MOCKED_MOVIES = 6
private const val POSTER_RATIO = 2f / 3f

@PreviewLightDark
@Composable
private fun WatchedMoviesScreenPreview() {
    val sampleMovies =
        List(NUMBER_OF_MOCKED_MOVIES) { index ->
            MovieUi(
                id = index,
                title = "Sample Movie $index",
                posterPath = null,
                watchedDate = "January 2024",
            )
        }
    val grouped = mapOf("January 2024" to sampleMovies)

    PreviewContentFullSize {
        WatchedMoviesScreen(
            uiState =
                WatchedMoviesUiState(
                    movies = grouped,
                    isLoading = false,
                    errorMessage = null,
                ),
            onProcess = {},
            onMovieClick = {},
        )
    }
}
