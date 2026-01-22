package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.noresultscontent.NoResultsContent
import com.asensiodev.core.designsystem.component.querytextfield.QueryTextField
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.searchmovies.impl.presentation.component.HeroMovieCard
import com.asensiodev.feature.searchmovies.impl.presentation.component.MovieCard
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import com.asensiodev.ui.LaunchEffectOnce
import kotlinx.coroutines.flow.distinctUntilChanged
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun SearchMoviesRoute(
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
        onLoadMorePopularMovies = viewModel::loadMorePopularMovies,
        onLoadMoreSearchedMovies = viewModel::loadMoreSearchResults,
        modifier = modifier,
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
            DashboardContent(
                uiState = uiState,
                onMovieClick = onMovieClick,
                onLoadMorePopular = onLoadMorePopularMovies,
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
private fun DashboardContent(
    uiState: SearchMoviesUiState,
    onMovieClick: (Int) -> Unit,
    onLoadMorePopular: () -> Unit,
) {
    if (uiState.screenState is SearchScreenState.Loading) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            LoadingIndicator()
        }
    } else if (uiState.screenState is SearchScreenState.Error) {
        ErrorContent(
            message = uiState.screenState.message,
            onRetry = {
                // TODO: Implement retry logic for dashboard
            },
        )
    } else {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing24),
        ) {
            if (uiState.nowPlayingMovies.isNotEmpty()) {
                NowPlayingSection(
                    movies = uiState.nowPlayingMovies,
                    onMovieClick = onMovieClick,
                )
            }

            if (uiState.popularMovies.isNotEmpty()) {
                MovieSection(
                    title = stringResource(SR.string.search_movies_popular_movies_title),
                    movies = uiState.popularMovies,
                    onMovieClick = onMovieClick,
                    onLoadMore = onLoadMorePopular,
                    isLoading = uiState.isPopularLoadingMore,
                )
            }

            if (uiState.topRatedMovies.isNotEmpty()) {
                MovieSection(
                    title = stringResource(SR.string.search_movies_top_rated_title),
                    movies = uiState.topRatedMovies,
                    onMovieClick = onMovieClick,
                )
            }

            if (uiState.upcomingMovies.isNotEmpty()) {
                MovieSection(
                    title = stringResource(SR.string.search_movies_upcoming_title),
                    movies = uiState.upcomingMovies,
                    onMovieClick = onMovieClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingSection(
    movies: List<MovieUi>,
    onMovieClick: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        Text(
            text = stringResource(SR.string.search_movies_now_playing_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacings.spacing16),
        )

        val carouselState = rememberCarouselState { movies.size }
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val heroItemWidth = screenWidth - Size.size48

        HorizontalMultiBrowseCarousel(
            state = carouselState,
            preferredItemWidth = heroItemWidth,
            itemSpacing = Spacings.spacing12,
            contentPadding =
                PaddingValues(
                    horizontal = Spacings.spacing16,
                ),
            modifier =
                Modifier
                    .width(screenWidth)
                    .height(Size.size260),
        ) { index ->
            val movie = movies[index]
            HeroMovieCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .maskClip(MaterialTheme.shapes.extraLarge),
            )
        }
    }
}

@Composable
private fun MovieSection(
    title: String,
    movies: List<MovieUi>,
    onMovieClick: (Int) -> Unit,
    onLoadMore: (() -> Unit)? = null,
    isLoading: Boolean = false,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
            contentPadding =
                PaddingValues(
                    end = Spacings.spacing16,
                ),
        ) {
            itemsIndexed(
                movies,
                key = { index, movie -> "$title-$index-${movie.id}" },
            ) { index, movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                    modifier =
                        Modifier
                            .width(Size.size120)
                            .height(Size.size180),
                )

                if (onLoadMore != null && index == movies.lastIndex && !isLoading) {
                    LaunchedEffect(Unit) {
                        onLoadMore()
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier =
                            Modifier
                                .height(Size.size180)
                                .width(Size.size50),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator()
                    }
                }
            }
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
        uiState.screenState is SearchScreenState.Loading -> {
            LoadingIndicator()
        }

        uiState.screenState is SearchScreenState.Error -> {
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
                isLoading = uiState.isSearchLoadingMore,
                isEndReached = uiState.isSearchEndReached,
            )
        }

        else -> {
            NoResultsContent(
                text = stringResource(SR.string.search_movies_no_search_results_text),
            )
        }
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
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(lazyGridState.isScrollInProgress) {
        if (lazyGridState.isScrollInProgress) {
            keyboardController?.hide()
        }
    }

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
                backdropPath = null,
                voteAverage = 7.5,
            )
        }

    PreviewContentFullSize {
        SearchMoviesScreen(
            uiState =
                SearchMoviesUiState(
                    nowPlayingMovies = sampleMovies,
                    popularMovies = sampleMovies,
                    topRatedMovies = sampleMovies,
                    upcomingMovies = sampleMovies,
                ),
            onQueryChanged = {},
            onMovieClick = {},
            onLoadMorePopularMovies = {},
            onLoadMoreSearchedMovies = {},
        )
    }
}

private const val LOAD_MORE_MOVIES_THRESHOLD = 5
private const val MOVIE_SAMPLE_LIST_SIZE = 10
private const val LOADING_GRID_ITEM_KEY = "loading_item"

private data class PaginationInfo(
    val lastVisibleIndex: Int,
    val totalItems: Int,
    val isCurrentlyLoading: Boolean,
    val hasReachedEnd: Boolean,
)
