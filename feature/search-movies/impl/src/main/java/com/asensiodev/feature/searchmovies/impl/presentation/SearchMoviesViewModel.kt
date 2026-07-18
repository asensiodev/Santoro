package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.data.repository.StaleDataException
import com.asensiodev.feature.searchmovies.impl.domain.model.FetchPolicy
import com.asensiodev.feature.searchmovies.impl.domain.usecase.ClearRecentSearchesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetMoviesByGenreUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetNowPlayingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetPopularMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetRecentSearchesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTopRatedMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTrendingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetUpcomingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SaveRecentSearchUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesByQueryAndGenreUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.presentation.mapper.toUiList
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import com.asensiodev.feature.searchmovies.impl.presentation.model.SectionType
import com.asensiodev.library.observability.api.NoOpObservabilityTracker
import com.asensiodev.library.observability.api.ObservabilityTracker
import com.asensiodev.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.asensiodev.santoro.core.stringresources.R as SR

@HiltViewModel
internal class SearchMoviesViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val searchMoviesUseCase: SearchMoviesUseCase,
        private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCase,
        private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
        private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase,
        private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase,
        private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
        private val getMoviesByGenreUseCase: GetMoviesByGenreUseCase,
        private val searchMoviesByQueryAndGenreUseCase: SearchMoviesByQueryAndGenreUseCase,
        private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
        private val saveRecentSearchUseCase: SaveRecentSearchUseCase,
        private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase,
        private val observabilityTracker: ObservabilityTracker = NoOpObservabilityTracker,
    ) : ViewModel() {
        private var searchJob: Job? = null
        private var dashboardJob: Job? = null
        private var queryJob: Job? = null
        private var initialized = false
        private var observersSetUp = false
        private var isDashboardStale = false
        private val restoredQuery = savedStateHandle[SEARCH_QUERY_KEY] ?: ""
        private val _uiState = MutableStateFlow(SearchMoviesUiState(query = restoredQuery))
        val uiState: StateFlow<SearchMoviesUiState> = _uiState.asStateFlow()

        private val _effect = MutableSharedFlow<SearchMoviesEffect>(extraBufferCapacity = 1)
        val effect = _effect.asSharedFlow()

        fun process(intent: SearchMoviesIntent) {
            when (intent) {
                is SearchMoviesIntent.LoadInitialData -> loadInitialData()
                is SearchMoviesIntent.Refresh -> refresh()
                is SearchMoviesIntent.UpdateQuery -> updateQuery(intent.query)
                is SearchMoviesIntent.SelectGenre -> onGenreSelected(intent.genreId)
                is SearchMoviesIntent.ClearGenre -> clearGenreSelection()
                is SearchMoviesIntent.SearchWithoutGenreFilter -> searchWithoutGenreFilter()
                is SearchMoviesIntent.LoadMoreSearchResults -> loadMoreSearchResults()
                is SearchMoviesIntent.LoadMorePopularMovies -> loadMorePopularMovies()
                is SearchMoviesIntent.SearchTriggered -> onSearchTriggered()
                is SearchMoviesIntent.MovieClicked -> onMovieClicked(intent.movieId)
                is SearchMoviesIntent.FieldFocused -> onFieldFocused()
                is SearchMoviesIntent.FieldCleared -> onFieldCleared()
                is SearchMoviesIntent.SuggestionTapped -> onSuggestionTapped(intent.query)
                is SearchMoviesIntent.ClearRecentSearches -> onClearRecentSearches()
                is SearchMoviesIntent.SeeAllClicked -> onSeeAllClicked(intent.sectionType)
            }
        }

        private fun loadInitialData() {
            if (initialized) return
            initialized = true
            observabilityTracker.trackScreen(SCREEN_SEARCH)
            setupObservers()
            if (restoredQuery.isBlank()) {
                fetchDashboardData()
            } else {
                performSearch(FIRST_PAGE, isInitialLoad = true)
            }
        }

        private fun setupObservers() {
            if (observersSetUp) return
            observersSetUp = true

            viewModelScope.launch {
                getRecentSearchesUseCase().collect { searches ->
                    _uiState.update { it.copy(recentSearches = searches) }
                }
            }
        }

        private fun refresh() {
            queryJob?.cancel()
            val query = _uiState.value.query
            val selectedGenreId = _uiState.value.selectedGenreId
            observabilityTracker.trackAction(
                SEARCH_REFRESH,
                mapOf(
                    HAS_QUERY to query.isNotBlank().toString(),
                    HAS_GENRE to (selectedGenreId != null).toString(),
                ),
            )
            _uiState.update { it.copy(isRefreshing = true) }
            if (query.isBlank() && selectedGenreId == null) {
                fetchDashboardData(fromRefresh = true)
            } else {
                performSearch(FIRST_PAGE, isInitialLoad = true, isFromRefresh = true)
            }
        }

        private fun updateQuery(query: String) {
            queryJob?.cancel()
            searchJob?.cancel()
            if (query.isNotBlank()) dashboardJob?.cancel()
            _uiState.update {
                it.copy(
                    query = query,
                    isShowingStaleData =
                        if (query.isBlank() && it.selectedGenreId == null) {
                            isDashboardStale
                        } else {
                            false
                        },
                    screenState =
                        when {
                            query.isBlank() &&
                                it.selectedGenreId == null -> SearchScreenState.Content
                            it.searchMovieResults.isEmpty() -> SearchScreenState.Loading
                            else -> it.screenState
                        },
                )
            }

            savedStateHandle[SEARCH_QUERY_KEY] = query
            queryJob =
                viewModelScope.launch {
                    delay(DELAY)
                    if (query == _uiState.value.query) handleQueryChange(query)
                }
        }

        private fun onGenreSelected(genreId: Int) {
            observabilityTracker.trackAction(
                SEARCH_GENRE_SELECTED,
                mapOf(
                    GENRE_ID to genreId.toString(),
                ),
            )
            queryJob?.cancel()
            searchJob?.cancel()
            dashboardJob?.cancel()
            _uiState.update {
                it.copy(
                    selectedGenreId = genreId,
                    searchMovieResults = emptyList(),
                    currentSearchPage = FIRST_PAGE,
                    isSearchEndReached = false,
                    isShowingStaleData = false,
                )
            }
            performSearch(FIRST_PAGE, isInitialLoad = true)
        }

        private fun clearGenreSelection() {
            queryJob?.cancel()
            val currentQuery = _uiState.value.query
            _uiState.update {
                it.copy(
                    selectedGenreId = null,
                    searchMovieResults = emptyList(),
                    currentSearchPage = FIRST_PAGE,
                    isSearchEndReached = false,
                    isShowingStaleData =
                        if (currentQuery.isBlank()) isDashboardStale else false,
                    screenState =
                        if (currentQuery.isBlank()) {
                            SearchScreenState.Content
                        } else {
                            it.screenState
                        },
                )
            }
            if (currentQuery.isNotBlank()) {
                performSearch(FIRST_PAGE, isInitialLoad = true)
            } else if (_uiState.value.hasDashboardData()) {
                _uiState.update { it.copy(screenState = SearchScreenState.Content) }
            } else {
                fetchDashboardData()
            }
        }

        private fun searchWithoutGenreFilter() {
            queryJob?.cancel()
            val currentQuery = _uiState.value.query
            _uiState.update {
                it.copy(
                    selectedGenreId = null,
                    searchMovieResults = emptyList(),
                    currentSearchPage = FIRST_PAGE,
                    isSearchEndReached = false,
                    isShowingStaleData = false,
                )
            }
            if (currentQuery.isNotBlank()) {
                performSearch(FIRST_PAGE, isInitialLoad = true)
            }
        }

        private fun handleQueryChange(query: String) {
            _uiState.update {
                it.copy(
                    searchMovieResults = emptyList(),
                    currentSearchPage = FIRST_PAGE,
                    isSearchEndReached = false,
                    isShowingStaleData =
                        if (query.isBlank() && it.selectedGenreId == null) {
                            isDashboardStale
                        } else {
                            false
                        },
                )
            }
            if (query.isBlank() && _uiState.value.selectedGenreId == null) {
                searchJob?.cancel()
                if (_uiState.value.hasDashboardData()) {
                    _uiState.update { it.copy(screenState = SearchScreenState.Content) }
                } else {
                    fetchDashboardData()
                }
            } else {
                performSearch(FIRST_PAGE, isInitialLoad = true)
            }
        }

        private fun performSearch(
            page: Int,
            isInitialLoad: Boolean,
            isFromRefresh: Boolean = false,
        ) {
            if (isInitialLoad) searchJob?.cancel()
            dashboardJob?.cancel()

            val query = _uiState.value.query
            val selectedGenreId = _uiState.value.selectedGenreId

            _uiState.update {
                it.copy(
                    screenState =
                        if (isInitialLoad && it.searchMovieResults.isEmpty()) {
                            SearchScreenState.Loading
                        } else {
                            it.screenState
                        },
                    isSearchLoadingMore = !isInitialLoad,
                )
            }

            searchJob =
                viewModelScope.launch {
                    val fetchPolicy =
                        if (isFromRefresh) FetchPolicy.REFRESH else FetchPolicy.CACHE_FIRST
                    val searchFlow =
                        when {
                            query.isNotBlank() && selectedGenreId != null -> {
                                searchMoviesByQueryAndGenreUseCase(
                                    query,
                                    selectedGenreId,
                                    page,
                                    fetchPolicy,
                                )
                            }

                            query.isNotBlank() -> {
                                searchMoviesUseCase(query, page, fetchPolicy)
                            }

                            selectedGenreId != null -> {
                                getMoviesByGenreUseCase(selectedGenreId, page)
                            }

                            else -> {
                                return@launch
                            }
                        }

                    val (result, isStale) = collectWithStale(searchFlow)
                    if (
                        query == _uiState.value.query &&
                        selectedGenreId == _uiState.value.selectedGenreId
                    ) {
                        handleSearchResult(
                            result = result,
                            isInitialLoad = isInitialLoad,
                            page = page,
                            isFromRefresh = isFromRefresh,
                            isStale = isStale,
                        )
                    }
                }
        }

        private suspend fun handleSearchResult(
            result: Result<List<Movie>>,
            isInitialLoad: Boolean,
            page: Int,
            isFromRefresh: Boolean = false,
            isStale: Boolean = false,
        ) {
            result.fold(
                onSuccess = { movies ->
                    val newMovies = movies.toUiList()

                    val updatedResults =
                        if (isInitialLoad) {
                            newMovies
                        } else {
                            _uiState.value.searchMovieResults + newMovies
                        }

                    val finalState =
                        if (isInitialLoad && newMovies.isEmpty()) {
                            SearchScreenState.Empty
                        } else {
                            SearchScreenState.Content
                        }

                    _uiState.update {
                        it.copy(
                            screenState = finalState,
                            isSearchLoadingMore = false,
                            isRefreshing = false,
                            isShowingStaleData =
                                isStale || (!isInitialLoad && it.isShowingStaleData),
                            searchMovieResults = updatedResults,
                            currentSearchPage = page,
                            isSearchEndReached = newMovies.isEmpty(),
                        )
                    }
                    if (isFromRefresh && !isStale) {
                        _effect.emit(SearchMoviesEffect.ShowRefreshSuccess)
                    }
                },
                onFailure = { exception ->
                    handleSearchFailure(exception, isInitialLoad, page, isFromRefresh)
                },
            )
        }

        private fun handleSearchFailure(
            exception: Throwable,
            isInitialLoad: Boolean,
            page: Int,
            isFromRefresh: Boolean,
        ) {
            if (exception is StaleDataException) {
                _uiState.update {
                    it.copy(
                        isShowingStaleData = true,
                        isSearchLoadingMore = false,
                        isRefreshing = false,
                    )
                }
                return
            }
            observabilityTracker.recordError(
                SEARCH_RESULTS_FAILED,
                exception,
                mapOf(IS_INITIAL_LOAD to isInitialLoad.toString(), PAGE to page.toString()),
            )
            _uiState.update {
                it.copy(
                    screenState =
                        if (isInitialLoad && it.searchMovieResults.isEmpty()) {
                            SearchScreenState.Error(
                                UiText.StringResource(SR.string.error_message_retry),
                            )
                        } else {
                            it.screenState
                        },
                    isSearchLoadingMore = false,
                    isRefreshing = false,
                    isShowingStaleData =
                        if (isInitialLoad) {
                            isFromRefresh && it.searchMovieResults.isNotEmpty()
                        } else {
                            it.isShowingStaleData
                        },
                    isSearchEndReached = true,
                )
            }
        }

        private fun loadMorePopularMovies() {
            if (!_uiState.value.isPopularLoadingMore && !_uiState.value.isPopularEndReached) {
                getPopularMovies()
            }
        }

        private fun loadMoreSearchResults() {
            val currentPage = _uiState.value.currentSearchPage
            val query = _uiState.value.query
            val selectedGenreId = _uiState.value.selectedGenreId

            if (!_uiState.value.isSearchLoadingMore && !_uiState.value.isSearchEndReached) {
                if (query.isNotBlank() || selectedGenreId != null) {
                    performSearch(currentPage + NEXT_PAGE, isInitialLoad = false)
                }
            }
        }

        private fun onSearchTriggered() {
            val query = _uiState.value.query
            if (query.isNotBlank()) {
                observabilityTracker.trackAction(
                    SEARCH_SUBMITTED,
                    mapOf(
                        QUERY_LENGTH to query.length.toString(),
                    ),
                )
                viewModelScope.launch { saveRecentSearchUseCase(query) }
            }
        }

        private fun onMovieClicked(movieId: Int) {
            val query = _uiState.value.query
            observabilityTracker.trackAction(
                SEARCH_MOVIE_CLICKED,
                mapOf(MOVIE_ID to movieId.toString(), HAS_QUERY to query.isNotBlank().toString()),
            )
            if (query.isNotBlank()) {
                viewModelScope.launch { saveRecentSearchUseCase(query) }
            }
            viewModelScope.launch {
                _effect.emit(SearchMoviesEffect.NavigateToDetail(movieId))
            }
        }

        private fun onFieldFocused() {
            _uiState.update { it.copy(isFieldFocused = true) }
        }

        private fun onFieldCleared() {
            _uiState.update { it.copy(isFieldFocused = false) }
        }

        private fun onSuggestionTapped(query: String) {
            observabilityTracker.trackAction(
                SEARCH_SUGGESTION_TAPPED,
                mapOf(
                    QUERY_LENGTH to query.length.toString(),
                ),
            )
            queryJob?.cancel()
            searchJob?.cancel()
            _uiState.update { it.copy(query = query, isFieldFocused = false) }
            savedStateHandle[SEARCH_QUERY_KEY] = query
            handleQueryChange(query)
            viewModelScope.launch { saveRecentSearchUseCase(query) }
        }

        private fun onClearRecentSearches() {
            observabilityTracker.trackAction(SEARCH_RECENT_CLEARED)
            viewModelScope.launch { clearRecentSearchesUseCase() }
        }

        private fun onSeeAllClicked(sectionType: SectionType) {
            observabilityTracker.trackAction(
                SEARCH_SEE_ALL_CLICKED,
                mapOf(
                    SECTION_TYPE to sectionType.name,
                ),
            )
            viewModelScope.launch {
                _effect.emit(SearchMoviesEffect.NavigateToSeeAll(sectionType))
            }
        }

        private fun fetchDashboardData(fromRefresh: Boolean = false) {
            dashboardJob?.cancel()
            if (!fromRefresh) {
                _uiState.update { it.copy(screenState = SearchScreenState.Loading) }
            }
            dashboardJob = viewModelScope.launch { loadDashboardData(fromRefresh) }
        }

        private suspend fun loadDashboardData(fromRefresh: Boolean) {
            try {
                val fetchPolicy =
                    if (fromRefresh) FetchPolicy.REFRESH else FetchPolicy.CACHE_FIRST
                val (results, isStale) = fetchDashboardResults(fetchPolicy)
                if (_uiState.value.query.isNotBlank() || _uiState.value.selectedGenreId != null) {
                    return
                }
                var refreshFailedWithExistingData = false

                _uiState.update { state ->
                    val movies = results.toUiLists()
                    val finalScreenState =
                        resolveDashboardScreenState(
                            movies.all,
                            results.all,
                        )
                    refreshFailedWithExistingData =
                        shouldKeepExistingDashboard(
                            fromRefresh = fromRefresh,
                            screenState = finalScreenState,
                            state = state,
                            results = results,
                        )
                    state.withDashboardResults(
                        movies = movies,
                        screenState = finalScreenState,
                        isStale = isStale,
                        keepExisting = refreshFailedWithExistingData,
                    )
                }
                isDashboardStale = isStale || refreshFailedWithExistingData
                if (fromRefresh && !refreshFailedWithExistingData && !isStale) {
                    _effect.emit(SearchMoviesEffect.ShowRefreshSuccess)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                observabilityTracker.recordError(SEARCH_DASHBOARD_FAILED, exception)
                if (_uiState.value.query.isBlank() && _uiState.value.selectedGenreId == null) {
                    _uiState.update { state -> state.withDashboardException() }
                    isDashboardStale = _uiState.value.isShowingStaleData
                }
            }
        }

        private suspend fun fetchDashboardResults(
            fetchPolicy: FetchPolicy,
        ): Pair<
            DashboardResults,
            Boolean,
        > =
            coroutineScope {
                val popularDeferred =
                    async {
                        collectWithStale(getPopularMoviesUseCase(FIRST_PAGE, fetchPolicy))
                    }
                val nowPlayingDeferred =
                    async {
                        collectWithStale(getNowPlayingMoviesUseCase(FIRST_PAGE, fetchPolicy))
                    }
                val topRatedDeferred =
                    async {
                        collectWithStale(getTopRatedMoviesUseCase(FIRST_PAGE, fetchPolicy))
                    }
                val upcomingDeferred =
                    async {
                        collectWithStale(getUpcomingMoviesUseCase(FIRST_PAGE, fetchPolicy))
                    }
                val trendingDeferred =
                    async {
                        collectWithStale(getTrendingMoviesUseCase(FIRST_PAGE, fetchPolicy))
                    }

                val (popularResult, popularStale) = popularDeferred.await()
                val (nowPlayingResult, nowPlayingStale) = nowPlayingDeferred.await()
                val (topRatedResult, topRatedStale) = topRatedDeferred.await()
                val (upcomingResult, upcomingStale) = upcomingDeferred.await()
                val (trendingResult, trendingStale) = trendingDeferred.await()

                val isStale =
                    popularStale ||
                        nowPlayingStale ||
                        topRatedStale ||
                        upcomingStale ||
                        trendingStale

                DashboardResults(
                    nowPlaying = nowPlayingResult,
                    popular = popularResult,
                    topRated = topRatedResult,
                    upcoming = upcomingResult,
                    trending = trendingResult,
                ) to isStale
            }

        private fun getPopularMovies() {
            _uiState.update { it.copy(isPopularLoadingMore = true) }

            viewModelScope.launch {
                val (result, isStale) =
                    collectWithStale(
                        getPopularMoviesUseCase(_uiState.value.currentPopularPage),
                    )
                result.fold(
                    onSuccess = { movies ->
                        val newMovies = movies.toUiList()
                        _uiState.update {
                            it.copy(
                                isPopularLoadingMore = false,
                                popularMovies = it.popularMovies + newMovies,
                                currentPopularPage = it.currentPopularPage + NEXT_PAGE,
                                isPopularEndReached = newMovies.isEmpty(),
                                isShowingStaleData = it.isShowingStaleData || isStale,
                            )
                        }
                        isDashboardStale = isDashboardStale || isStale
                    },
                    onFailure = {
                        observabilityTracker.recordError(SEARCH_POPULAR_LOAD_MORE_FAILED, it)
                        _uiState.update { state ->
                            state.copy(
                                isPopularLoadingMore = false,
                                isPopularEndReached = true,
                            )
                        }
                    },
                )
            }
        }
    }

private data class DashboardResults(
    val nowPlaying: Result<List<Movie>>,
    val popular: Result<List<Movie>>,
    val topRated: Result<List<Movie>>,
    val upcoming: Result<List<Movie>>,
    val trending: Result<List<Movie>>,
) {
    val all: List<Result<List<Movie>>>
        get() = listOf(nowPlaying, popular, topRated, upcoming, trending)
}

private data class DashboardMovies(
    val nowPlaying: List<MovieUi>,
    val popular: List<MovieUi>,
    val topRated: List<MovieUi>,
    val upcoming: List<MovieUi>,
    val trending: List<MovieUi>,
) {
    val all: List<List<MovieUi>>
        get() = listOf(nowPlaying, popular, topRated, upcoming, trending)
}

private fun DashboardResults.toUiLists(): DashboardMovies =
    DashboardMovies(
        nowPlaying = nowPlaying.getOrDefault(emptyList()).toUiList(),
        popular = popular.getOrDefault(emptyList()).toUiList(),
        topRated = topRated.getOrDefault(emptyList()).toUiList(),
        upcoming = upcoming.getOrDefault(emptyList()).toUiList(),
        trending = trending.getOrDefault(emptyList()).toUiList(),
    )

private fun shouldKeepExistingDashboard(
    fromRefresh: Boolean,
    screenState: SearchScreenState,
    state: SearchMoviesUiState,
    results: DashboardResults,
): Boolean =
    fromRefresh &&
        screenState !is SearchScreenState.Content &&
        state.hasDashboardData() &&
        results.all.any { result -> result.isFailure }

private fun SearchMoviesUiState.withDashboardResults(
    movies: DashboardMovies,
    screenState: SearchScreenState,
    isStale: Boolean,
    keepExisting: Boolean,
): SearchMoviesUiState =
    copy(
        screenState = if (keepExisting) SearchScreenState.Content else screenState,
        isShowingStaleData = isStale || keepExisting,
        isRefreshing = false,
        nowPlayingMovies = if (keepExisting) nowPlayingMovies else movies.nowPlaying,
        popularMovies = if (keepExisting) popularMovies else movies.popular,
        topRatedMovies = if (keepExisting) topRatedMovies else movies.topRated,
        upcomingMovies = if (keepExisting) upcomingMovies else movies.upcoming,
        trendingMovies = if (keepExisting) trendingMovies else movies.trending,
        trendingSuggestions =
            if (keepExisting) {
                trendingSuggestions
            } else {
                movies.trendingSuggestions()
            },
        currentPopularPage = if (keepExisting) currentPopularPage else movies.nextPopularPage(),
        isPopularEndReached = if (keepExisting) isPopularEndReached else movies.popular.isEmpty(),
    )

private fun SearchMoviesUiState.withDashboardException(): SearchMoviesUiState =
    copy(
        screenState =
            if (hasDashboardData()) {
                SearchScreenState.Content
            } else {
                SearchScreenState.Error(UiText.StringResource(SR.string.error_message_retry))
            },
        isShowingStaleData = hasDashboardData(),
        isRefreshing = false,
    )

private fun DashboardMovies.trendingSuggestions(): List<String> =
    trending
        .mapNotNull { movie -> movie.title.takeIf { title -> title.isNotBlank() } }
        .take(TRENDING_SUGGESTIONS_LIMIT)

private fun DashboardMovies.nextPopularPage(): Int =
    if (popular.isNotEmpty()) {
        FIRST_PAGE + NEXT_PAGE
    } else {
        FIRST_PAGE
    }

private fun resolveDashboardScreenState(
    movieLists: List<List<MovieUi>>,
    results: List<Result<List<Movie>>>,
): SearchScreenState {
    val hasAnyData = movieLists.any { it.isNotEmpty() }
    val hasAnySuccess = results.any { result -> result.isSuccess }
    return when {
        hasAnyData -> SearchScreenState.Content
        hasAnySuccess -> SearchScreenState.Empty
        else ->
            SearchScreenState.Error(
                UiText.StringResource(SR.string.error_message_retry),
            )
    }
}

private fun SearchMoviesUiState.hasDashboardData(): Boolean =
    listOf(
        nowPlayingMovies,
        popularMovies,
        topRatedMovies,
        upcomingMovies,
        trendingMovies,
    ).any { movies -> movies.isNotEmpty() }

internal suspend fun collectWithStale(
    flow: kotlinx.coroutines.flow.Flow<Result<List<Movie>>>,
): Pair<Result<List<Movie>>, Boolean> {
    var data: Result<List<Movie>> = Result.failure(IllegalStateException("No dashboard data"))
    var stale = false
    flow.collect { result ->
        when {
            result.isSuccess -> data = result
            result.isFailure &&
                result.exceptionOrNull() is StaleDataException -> stale = true
            result.isFailure -> data = result
        }
    }
    return data to stale
}

private const val DELAY: Long = 500
private const val FIRST_PAGE: Int = 1
private const val NEXT_PAGE: Int = 1
private const val SEARCH_QUERY_KEY = "search_query"
private const val TRENDING_SUGGESTIONS_LIMIT = 10
private const val SCREEN_SEARCH = "search"
private const val HAS_QUERY = "has_query"
private const val HAS_GENRE = "has_genre"
private const val GENRE_ID = "genre_id"
private const val QUERY_LENGTH = "query_length"
private const val MOVIE_ID = "movie_id"
private const val SECTION_TYPE = "section_type"
private const val IS_INITIAL_LOAD = "is_initial_load"
private const val PAGE = "page"
private const val SEARCH_REFRESH = "search_refresh"
private const val SEARCH_GENRE_SELECTED = "search_genre_selected"
private const val SEARCH_SUBMITTED = "search_submitted"
private const val SEARCH_MOVIE_CLICKED = "search_movie_clicked"
private const val SEARCH_SUGGESTION_TAPPED = "search_suggestion_tapped"
private const val SEARCH_RECENT_CLEARED = "search_recent_cleared"
private const val SEARCH_SEE_ALL_CLICKED = "search_see_all_clicked"
private const val SEARCH_DASHBOARD_FAILED = "search_dashboard_failed"
private const val SEARCH_RESULTS_FAILED = "search_results_failed"
private const val SEARCH_POPULAR_LOAD_MORE_FAILED = "search_popular_load_more_failed"
