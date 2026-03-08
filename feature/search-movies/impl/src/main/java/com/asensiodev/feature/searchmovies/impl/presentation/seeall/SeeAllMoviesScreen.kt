package com.asensiodev.feature.searchmovies.impl.presentation.seeall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.noresultscontent.NoResultsContent
import com.asensiodev.core.designsystem.component.topbar.SantoroAppBar
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.searchmovies.impl.presentation.component.MovieCard
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import com.asensiodev.feature.searchmovies.impl.presentation.model.SectionType
import kotlinx.coroutines.flow.distinctUntilChanged
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun SeeAllMoviesRoute(
    onMovieClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SeeAllMoviesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.process(SeeAllMoviesIntent.LoadInitial)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SeeAllMoviesEffect.NavigateToDetail -> onMovieClick(effect.movieId)
            }
        }
    }

    val onProcess = remember(viewModel) { viewModel::process }

    SeeAllMoviesScreen(
        uiState = uiState,
        onProcess = onProcess,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
internal fun SeeAllMoviesScreen(
    uiState: SeeAllMoviesUiState,
    onProcess: (SeeAllMoviesIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SantoroAppBar(
        title = stringResource(uiState.sectionType.titleRes),
        onBackClicked = onBackClick,
        modifier = modifier,
    ) {
        when (uiState.screenState) {
            is SeeAllScreenState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }

            is SeeAllScreenState.Error -> {
                ErrorContent(
                    message = stringResource(SR.string.error_message_retry),
                    onRetry = { onProcess(SeeAllMoviesIntent.Retry) },
                )
            }

            is SeeAllScreenState.Empty -> {
                NoResultsContent(
                    text = stringResource(SR.string.search_movies_no_search_results_text),
                )
            }

            is SeeAllScreenState.Content -> {
                SeeAllMovieGrid(
                    movies = uiState.movies,
                    isLoadingMore = uiState.isLoadingMore,
                    isEndReached = uiState.isEndReached,
                    onMovieClick = { onProcess(SeeAllMoviesIntent.MovieClicked(it)) },
                    onLoadMore = { onProcess(SeeAllMoviesIntent.LoadMore) },
                )
            }
        }
    }
}

@Composable
private fun SeeAllMovieGrid(
    movies: List<MovieUi>,
    isLoadingMore: Boolean,
    isEndReached: Boolean,
    onMovieClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyGridState = rememberLazyGridState()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = Size.size112),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        modifier = modifier.fillMaxSize(),
        state = lazyGridState,
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(Spacings.spacing16),
    ) {
        itemsIndexed(
            movies,
            key = { index, movie -> "$index-${movie.id}" },
        ) { _, movie ->
            MovieCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) },
                modifier = Modifier.aspectRatio(POSTER_ASPECT_RATIO),
            )
        }

        if (isLoadingMore && !isEndReached) {
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

    ObserveSeeAllGridState(lazyGridState, isLoadingMore, isEndReached, onLoadMore)
}

@Composable
private fun ObserveSeeAllGridState(
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
            SeeAllPaginationInfo(
                lastVisibleIndex = lastVisibleItem,
                totalItems = totalItems,
                isCurrentlyLoading = isLoading,
                hasReachedEnd = isEndReached,
            )
        }.distinctUntilChanged()
            .collect { paginationInfo ->
                val shouldLoadMore =
                    paginationInfo.lastVisibleIndex >=
                        paginationInfo.totalItems - LOAD_MORE_THRESHOLD &&
                        !paginationInfo.isCurrentlyLoading &&
                        !paginationInfo.hasReachedEnd &&
                        paginationInfo.totalItems > 0

                if (shouldLoadMore) {
                    onLoadMore()
                }
            }
    }
}

@PreviewLightDark
@Composable
private fun SeeAllMoviesScreenContentPreview() {
    val sampleMovies =
        List(PREVIEW_SAMPLE_SIZE) { index ->
            MovieUi(
                id = index,
                title = "Sample Movie $index",
                posterPath = null,
                backdropPath = null,
                voteAverage = 7.5,
            )
        }

    PreviewContentFullSize {
        SeeAllMoviesScreen(
            uiState =
                SeeAllMoviesUiState(
                    sectionType = SectionType.TRENDING,
                    movies = sampleMovies,
                    screenState = SeeAllScreenState.Content,
                ),
            onProcess = {},
            onBackClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SeeAllMoviesScreenLoadingPreview() {
    PreviewContentFullSize {
        SeeAllMoviesScreen(
            uiState =
                SeeAllMoviesUiState(
                    sectionType = SectionType.POPULAR,
                    screenState = SeeAllScreenState.Loading,
                ),
            onProcess = {},
            onBackClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SeeAllMoviesScreenErrorPreview() {
    PreviewContentFullSize {
        SeeAllMoviesScreen(
            uiState =
                SeeAllMoviesUiState(
                    sectionType = SectionType.TOP_RATED,
                    screenState = SeeAllScreenState.Error("Network error"),
                ),
            onProcess = {},
            onBackClick = {},
        )
    }
}

private const val POSTER_ASPECT_RATIO = 2f / 3f
private const val LOAD_MORE_THRESHOLD = 5
private const val LOADING_GRID_ITEM_KEY = "see_all_loading_item"
private const val PREVIEW_SAMPLE_SIZE = 12

private data class SeeAllPaginationInfo(
    val lastVisibleIndex: Int,
    val totalItems: Int,
    val isCurrentlyLoading: Boolean,
    val hasReachedEnd: Boolean,
)
