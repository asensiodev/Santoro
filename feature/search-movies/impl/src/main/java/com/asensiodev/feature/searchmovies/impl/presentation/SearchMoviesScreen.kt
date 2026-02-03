package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.noresultscontent.NoResultsContent
import com.asensiodev.core.designsystem.component.querytextfield.QueryTextField
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.designsystem.theme.Weights
import com.asensiodev.feature.searchmovies.impl.presentation.component.HeroMovieCard
import com.asensiodev.feature.searchmovies.impl.presentation.component.MovieCard
import com.asensiodev.feature.searchmovies.impl.presentation.model.GenreConstants
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
        onGenreSelected = viewModel::onGenreSelected,
        onClearGenreSelection = viewModel::clearGenreSelection,
        onSearchWithoutGenreFilter = viewModel::searchWithoutGenreFilter,
        modifier = modifier,
    )
}

@Composable
internal fun SearchMoviesScreen(
    uiState: SearchMoviesUiState,
    onQueryChanged: (String) -> Unit,
    onMovieClick: (Int) -> Unit,
    onGenreSelected: (Int) -> Unit,
    onClearGenreSelection: () -> Unit,
    onSearchWithoutGenreFilter: () -> Unit,
    modifier: Modifier = Modifier,
    onLoadMorePopularMovies: () -> Unit,
    onLoadMoreSearchedMovies: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                        },
                    )
                }.padding(Spacings.spacing16),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        QueryTextField(
            query = uiState.query,
            placeholder = stringResource(SR.string.search_movies_textfield_placeholder),
            onQueryChanged = onQueryChanged,
        )

        GenreFilterChips(
            selectedGenreId = uiState.selectedGenreId,
            onGenreSelected = onGenreSelected,
            onClearGenre = onClearGenreSelection,
        )

        if (uiState.query.isBlank() && uiState.selectedGenreId == null) {
            DashboardContent(
                uiState = uiState,
                onMovieClick = onMovieClick,
                onLoadMorePopular = onLoadMorePopularMovies,
            )
        } else if (uiState.selectedGenreId != null) {
            SearchMoviesContent(
                uiState = uiState,
                onQueryChanged = onQueryChanged,
                onMovieClick = onMovieClick,
                onLoadMore = onLoadMoreSearchedMovies,
                onSearchWithoutGenreFilter = onSearchWithoutGenreFilter,
            )
        } else {
            SearchMoviesContent(
                uiState = uiState,
                onQueryChanged = onQueryChanged,
                onMovieClick = onMovieClick,
                onLoadMore = onLoadMoreSearchedMovies,
                onSearchWithoutGenreFilter = onSearchWithoutGenreFilter,
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

            if (uiState.trendingMovies.isNotEmpty()) {
                MovieSection(
                    title = stringResource(SR.string.search_movies_trending_title),
                    movies = uiState.trendingMovies,
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
        )

        val carouselState = rememberCarouselState { movies.size }
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val heroItemWidth = screenWidth - Size.size48

        HorizontalMultiBrowseCarousel(
            state = carouselState,
            preferredItemWidth = heroItemWidth,
            itemSpacing = Spacings.spacing12,
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
    onSearchWithoutGenreFilter: () -> Unit,
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

        uiState.screenState is SearchScreenState.Empty -> {
            EmptySearchWithFiltersContent(
                query = uiState.query,
                hasGenreFilter = uiState.selectedGenreId != null,
                onSearchWithoutGenreFilter = onSearchWithoutGenreFilter,
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
        columns = GridCells.Adaptive(minSize = Size.size112),
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
                modifier = Modifier.aspectRatio(POSTER_ASPECT_RATIO),
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
private fun GenreFilterChips(
    selectedGenreId: Int?,
    onGenreSelected: (Int) -> Unit,
    onClearGenre: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    var genreToShow by remember { mutableStateOf<Int?>(null) }

    if (selectedGenreId != null) {
        genreToShow = selectedGenreId
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = selectedGenreId != null,
            enter =
                fadeIn(animationSpec = tween(ANIMATION_DURATION_MS)) +
                    expandHorizontally(animationSpec = tween(ANIMATION_DURATION_MS)),
            exit =
                fadeOut(
                    animationSpec = tween(ANIMATION_DURATION_MS),
                ) + shrinkHorizontally(animationSpec = tween(ANIMATION_DURATION_MS)),
        ) {
            val selectedGenre = GenreConstants.availableGenres.find { it.id == genreToShow }
            if (selectedGenre != null) {
                FilterChip(
                    selected = true,
                    onClick = {
                        focusManager.clearFocus()
                        onClearGenre()
                    },
                    label = { Text(text = stringResource(selectedGenre.nameRes)) },
                    trailingIcon = {
                        Icon(
                            imageVector = AppIcons.Clear,
                            contentDescription =
                                stringResource(
                                    SR.string.query_text_field_clear_button_description,
                                ),
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTrailingIconColor =
                                MaterialTheme.colorScheme
                                    .onPrimaryContainer,
                        ),
                    shape = RoundedCornerShape(Size.size16),
                    modifier = Modifier.padding(horizontal = Spacings.spacing4),
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
            contentPadding = PaddingValues(horizontal = Spacings.spacing4),
            modifier = Modifier.weight(Weights.W10),
        ) {
            items(
                items = GenreConstants.availableGenres,
                key = { it.id },
            ) { genre ->
                val isSelected = genre.id == selectedGenreId
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        focusManager.clearFocus()
                        if (isSelected) {
                            onClearGenre()
                        } else {
                            onGenreSelected(genre.id)
                        }
                    },
                    label = { Text(text = stringResource(genre.nameRes)) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    shape = RoundedCornerShape(Size.size16),
                )
            }
        }
    }
}

@Composable
private fun EmptySearchWithFiltersContent(
    query: String,
    hasGenreFilter: Boolean,
    onSearchWithoutGenreFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacings.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing24, Alignment.CenterVertically),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
        ) {
            Icon(
                imageVector = AppIcons.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(Size.size120),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )

            if (hasGenreFilter && query.isNotBlank()) {
                Text(
                    text =
                        stringResource(
                            SR.string.search_movies_no_results_with_filters_message,
                            query,
                        ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    text = stringResource(SR.string.search_movies_no_search_results_text),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (hasGenreFilter && query.isNotBlank()) {
            Button(
                onClick = onSearchWithoutGenreFilter,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(SR.string.search_movies_remove_genre_filter_button))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun EmptySearchWithFiltersContentPreview() {
    PreviewContentFullSize {
        EmptySearchWithFiltersContent(
            query = "casino",
            hasGenreFilter = true,
            onSearchWithoutGenreFilter = {},
        )
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
            onGenreSelected = {},
            onClearGenreSelection = {},
            onSearchWithoutGenreFilter = {},
        )
    }
}

private const val LOAD_MORE_MOVIES_THRESHOLD = 5
private const val MOVIE_SAMPLE_LIST_SIZE = 10
private const val LOADING_GRID_ITEM_KEY = "loading_item"
private const val POSTER_ASPECT_RATIO = 2f / 3f
private const val ANIMATION_DURATION_MS = 300

private data class PaginationInfo(
    val lastVisibleIndex: Int,
    val totalItems: Int,
    val isCurrentlyLoading: Boolean,
    val hasReachedEnd: Boolean,
)
