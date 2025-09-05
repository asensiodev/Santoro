package com.asensiodev.feature.watchlist.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.asensiodev.feature.watchlist.impl.presentation.component.MovieCard
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun WatchlistMoviesRoute(
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchlistMoviesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WatchlistMoviesScreen(
        uiState = uiState,
        onQueryChanged = viewModel::updateQuery,
        onMovieClick = onMovieClick,
        modifier = modifier,
    )
}

// 🔹 UI pura sin ViewModel (usada por previews y el entrypoint)
@Composable
internal fun WatchlistMoviesScreen(
    uiState: WatchlistMoviesUiState,
    onQueryChanged: (String) -> Unit,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacings.spacing16),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        QueryTextField(
            query = uiState.query,
            placeholder = stringResource(SR.string.watchlist_movies_textfield_placeholder),
            onQueryChanged = onQueryChanged,
        )
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.errorMessage != null -> {
                ErrorContent(
                    message = stringResource(SR.string.error_message_retry),
                    onRetry = { onQueryChanged(uiState.query) },
                )
            }

            uiState.hasResults -> {
                MovieList(
                    movies = uiState.movies,
                    onMovieClick = onMovieClick,
                )
            }

            else ->
                NoResultsContent(
                    text = stringResource(SR.string.search_movies_no_search_results_text),
                )
        }
    }
}

// Don't move to commons because I want to customize for each screen
@Composable
fun MovieList(
    movies: List<MovieUi>,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = Size.size88),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        modifier = modifier,
    ) {
        items(movies) { movie ->
            MovieCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun WatchlistdMoviesScreenPreview() {
    val sampleMovies =
        List(MOVIE_SAMPLE_LIST_SIZE) { index ->
            MovieUi(
                id = index,
                title = "Sample Movie $index",
                posterPath = null,
            )
        }

    PreviewContentFullSize {
        WatchlistMoviesScreen(
            uiState =
                WatchlistMoviesUiState(
                    movies = sampleMovies,
                    isLoading = false,
                    errorMessage = null,
                    hasResults = true,
                ),
            onQueryChanged = {},
            onMovieClick = {},
        )
    }
}

private const val MOVIE_SAMPLE_LIST_SIZE = 5
