package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.asensiodev.feature.searchmovies.impl.presentation.component.MovieCard
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import com.asensiodev.ui.LaunchEffectOnce
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import com.asensiodev.santoro.core.stringresources.R as SR

class SearchMoviesScreen
    @Inject
    constructor() {
        @Composable
        fun Screen(onMovieClick: (Int) -> Unit) {
            SearchMoviesRoot(
                onMovieClick = onMovieClick,
            )
        }
    }

@Composable
internal fun SearchMoviesRoot(
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchMoviesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchEffectOnce {
        viewModel.loadInitialData()
    }

    SearchMoviesScreen(
        uiState = uiState,
        onQueryChanged = viewModel::updateQuery,
        onMovieClick = onMovieClick,
        modifier = modifier,
        onLoadMorePopularMovies = {
            viewModel.loadMorePopularMovies()
        },
        onLoadMoreSearchedMovies = {
            viewModel.loadMoreSearchResults()
        },
    )
}

@Composable
internal fun SearchMoviesScreen(
    uiState: SearchMoviesUiState,
    onQueryChanged: (String) -> Unit,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onLoadMorePopularMovies: () -> Unit,
    onLoadMoreSearchedMovies: () -> Unit,
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
            placeholder = stringResource(SR.string.search_movies_textfield_placeholder),
            onQueryChanged = onQueryChanged,
        )
        if (uiState.query.isBlank()) {
            PopularMoviesContent(
                uiState = uiState,
                onMovieClick = onMovieClick,
                onLoadMore = onLoadMorePopularMovies,
            )
        } else {
            SearchMoviesContent(
                uiState = uiState,
                onQueryChanged = onQueryChanged,
                onMovieClick = onMovieClick,
                onLoadMore = onLoadMoreSearchedMovies,
            )
        }
    }
}

@Composable
private fun PopularMoviesContent(
    uiState: SearchMoviesUiState,
    onMovieClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
) {
    when {
        uiState.isInitialLoading -> {
            LoadingIndicator()
        }

        uiState.errorMessage != null && !uiState.hasPopularMoviesResults -> {
            NoResultsContent(
                text = stringResource(SR.string.search_movies_no_popular_movies_results_text),
            )
        }

        uiState.hasPopularMoviesResults -> {
            Text(
                text =
                    stringResource(
                        SR.string.search_movies_popular_movies_title,
                    ),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            MovieList(
                movies = uiState.popularMovies,
                onMovieClick = onMovieClick,
                onLoadMore = onLoadMore,
                isLoading = uiState.isPopularMoviesLoading,
                isEndReached = uiState.isPopularEndReached,
            )
        }

        else -> {
            NoResultsContent(
                text = stringResource(SR.string.search_movies_no_popular_movies_results_text),
            )
        }
    }
}

@Composable
private fun SearchMoviesContent(
    uiState: SearchMoviesUiState,
    onQueryChanged: (String) -> Unit,
    onMovieClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
) {
    when {
        uiState.isInitialLoading -> LoadingIndicator()
        uiState.errorMessage != null && !uiState.hasSearchResults -> {
            ErrorContent(
                message = stringResource(SR.string.error_message_retry),
                onRetry = { onQueryChanged(uiState.query) },
            )
        }

        uiState.hasSearchResults -> {
            MovieList(
                movies = uiState.searchMovieResults,
                onMovieClick = onMovieClick,
                onLoadMore = onLoadMore,
                isLoading = uiState.isSearchLoading,
                isEndReached = uiState.isSearchEndReached,
            )
        }

        else ->
            NoResultsContent(
                text = stringResource(SR.string.search_movies_no_search_results_text),
            )
    }
}

@Composable
private fun MovieList(
    movies: List<MovieUi>,
    onMovieClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    isEndReached: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val lazyGridState = rememberLazyGridState()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = Size.size88),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        modifier = modifier,
        state = lazyGridState,
    ) {
        itemsIndexed(
            movies,
            key = { index, movie -> generateUniqueKey(index, movie) },
        ) { _, movie ->
            MovieCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) },
            )
        }
        if (isLoading && !isEndReached) {
            item(
                key = LOADING_GRID_ITEM_KEY,
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(Spacings.spacing8),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }
        }
    }

    ObserveGridState(lazyGridState, isLoading, isEndReached, onLoadMore)
}

@Composable
private fun ObserveGridState(
    lazyGridState: LazyGridState,
    isLoading: Boolean,
    isEndReached: Boolean,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(lazyGridState) {
        snapshotFlow {
            val lastVisibleItem =
                lazyGridState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: 0
            val totalItems = lazyGridState.layoutInfo.totalItemsCount
            PaginationInfo(
                lastVisibleIndex = lastVisibleItem,
                totalItems = totalItems,
                isCurrentlyLoading = isLoading,
                hasReachedEnd = isEndReached,
            )
        }.distinctUntilChanged()
            .collect { paginationInfo ->
                val shouldLoadMore =
                    paginationInfo.lastVisibleIndex >=
                        paginationInfo.totalItems - LOAD_MORE_MOVIES_THRESHOLD &&
                        !paginationInfo.isCurrentlyLoading &&
                        !paginationInfo.hasReachedEnd &&
                        paginationInfo.totalItems > 0

                if (shouldLoadMore) {
                    onLoadMore()
                }
            }
    }
}

private fun generateUniqueKey(
    index: Int,
    movie: MovieUi,
) = "$index-${movie.id}"

@PreviewLightDark
@Composable
private fun SearchMoviesScreenPreview() {
    val sampleMovies =
        List(MOVIE_SAMPLE_LIST_SIZE) { index ->
            MovieUi(
                id = index,
                title = "Sample Movie $index",
                posterPath = null,
            )
        }

    PreviewContentFullSize {
        SearchMoviesScreen(
            uiState =
                SearchMoviesUiState(
                    query = "",
                    popularMovies = sampleMovies,
                    isSearchLoading = false,
                    errorMessage = null,
                    hasPopularMoviesResults = true,
                ),
            onQueryChanged = {},
            onMovieClick = {},
            onLoadMorePopularMovies = {},
            onLoadMoreSearchedMovies = {},
        )
    }
}

private const val MOVIE_SAMPLE_LIST_SIZE = 5
private const val LOAD_MORE_MOVIES_THRESHOLD = 4
private const val LOADING_GRID_ITEM_KEY = "loading_item_key"

private data class PaginationInfo(
    val lastVisibleIndex: Int,
    val totalItems: Int,
    val isCurrentlyLoading: Boolean,
    val hasReachedEnd: Boolean,
)
