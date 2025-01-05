package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.noresultscontent.NoResultsContent
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import com.asensiodev.santoro.core.designsystem.R as DR
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
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

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
        QueryTextField(uiState, onQueryChanged)
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
                message = stringResource(SR.string.search_movies_no_search_results_text),
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
private fun QueryTextField(
    uiState: SearchMoviesUiState,
    onQueryChanged: (String) -> Unit,
) {
    TextField(
        value = uiState.query,
        onValueChange = onQueryChanged,
        placeholder = { Text(stringResource(SR.string.search_movies_textfield_placeholder)) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            if (uiState.query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged(EMPTY_STRING) }) {
                    Icon(
                        imageVector = AppIcons.ClearIcon,
                        contentDescription =
                            stringResource(
                                SR.string.search_movies_query_text_field_clear_button_description,
                            ),
                    )
                }
            }
        },
    )
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

@Composable
fun MovieCard(
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
private fun SearchMoviesScreenPreview() {
    val sampleMovies =
        List(MOVIE_SAMPLE_LIST_SIZE) { index ->
            MovieUi(
                id = index,
                title = "Sample Movie $index",
                posterPath = null,
            )
        }

    PreviewContent {
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

@PreviewLightDark
@Composable
private fun MovieCardPreview() {
    val sampleMovie =
        MovieUi(
            id = 1,
            title = "Sample Movie",
            posterPath = null,
        )

    PreviewContent {
        MovieCard(
            movie = sampleMovie,
            onClick = {},
        )
    }
}

private const val MOVIE_SAMPLE_LIST_SIZE = 5
private const val EMPTY_STRING = ""
private const val LOAD_MORE_MOVIES_THRESHOLD = 4
private const val FULL_WEIGHT = 1f
private const val LOADING_GRID_ITEM_KEY = "loading_item_key"

private data class PaginationInfo(
    val lastVisibleIndex: Int,
    val totalItems: Int,
    val isCurrentlyLoading: Boolean,
    val hasReachedEnd: Boolean,
)
