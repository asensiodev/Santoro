package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.data.repository.CachingSearchMoviesRepository
import com.asensiodev.feature.searchmovies.impl.data.repository.StaleDataException
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
        private val cachingRepository: CachingSearchMoviesRepository,
        private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
        private val saveRecentSearchUseCase: SaveRecentSearchUseCase,
        private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase,
    ) : ViewModel() {
        private var searchJob: Job? = null
        private val _uiState = MutableStateFlow(SearchMoviesUiState())
        val uiState: StateFlow<SearchMoviesUiState> = _uiState.asStateFlow()

        private val _effect = Channel<SearchMoviesEffect>(Channel.BUFFERED)
        val effect = _effect.receiveAsFlow()

        private val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY_KEY, "")

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
            }
        }

        @OptIn(FlowPreview::class)
        private fun loadInitialData() {
            fetchDashboardData()

            viewModelScope.launch {
                getRecentSearchesUseCase().collect { searches ->
                    _uiState.update { it.copy(recentSearches = searches) }
                }
            }

            searchQuery
                .debounce(DELAY)
                .distinctUntilChanged()
                .onEach { query ->
                    handleQueryChange(query)
                }.launchIn(viewModelScope)
        }

        private fun refresh() {
            val query = _uiState.value.query
            val selectedGenreId = _uiState.value.selectedGenreId
            _uiState.update { it.copy(isRefreshing = true) }
            viewModelScope.launch {
                cachingRepository.clearAllSections()
                if (query.isBlank() && selectedGenreId == null) {
                    fetchDashboardData(fromRefresh = true)
                } else {
                    performSearch(FIRST_PAGE, isInitialLoad = true, isFromRefresh = true)
                }
            }
        }

        private fun updateQuery(query: String) {
            _uiState.update { it.copy(query = query) }

            savedStateHandle[SEARCH_QUERY_KEY] = query
        }

        private fun onGenreSelected(genreId: Int) {
            searchJob?.cancel()
            _uiState.update {
                it.copy(
                    selectedGenreId = genreId,
                    searchMovieResults = emptyList(),
                    currentSearchPage = FIRST_PAGE,
                    isSearchEndReached = false,
                )
            }
            performSearch(FIRST_PAGE, isInitialLoad = true)
        }

        private fun clearGenreSelection() {
            val currentQuery = _uiState.value.query
            _uiState.update {
                it.copy(
                    selectedGenreId = null,
                    searchMovieResults = emptyList(),
                    currentSearchPage = FIRST_PAGE,
                    isSearchEndReached = false,
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
            }
        }

        private fun searchWithoutGenreFilter() {
            val currentQuery = _uiState.value.query
            _uiState.update {
                it.copy(
                    selectedGenreId = null,
                    searchMovieResults = emptyList(),
                    currentSearchPage = FIRST_PAGE,
                    isSearchEndReached = false,
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
                )
            }
            if (query.isBlank() && _uiState.value.selectedGenreId == null) {
                _uiState.update { it.copy(screenState = SearchScreenState.Content) }
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
                    val searchFlow =
                        when {
                            query.isNotBlank() && selectedGenreId != null -> {
                                searchMoviesByQueryAndGenreUseCase(query, selectedGenreId, page)
                            }

                            query.isNotBlank() -> {
                                searchMoviesUseCase(query, page)
                            }

                            selectedGenreId != null -> {
                                getMoviesByGenreUseCase(selectedGenreId, page)
                            }

                            else -> {
                                return@launch
                            }
                        }

                    searchFlow.collect { result ->
                        handleSearchResult(result, isInitialLoad, page, isFromRefresh)
                    }
                }
        }

        private fun handleSearchResult(
            result: Result<List<Movie>>,
            isInitialLoad: Boolean,
            page: Int,
            isFromRefresh: Boolean = false,
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
                            isShowingStaleData = false,
                            searchMovieResults = updatedResults,
                            currentSearchPage = page,
                            isSearchEndReached = newMovies.isEmpty(),
                        )
                    }
                    if (isFromRefresh) _effect.trySend(SearchMoviesEffect.ShowRefreshSuccess)
                },
                onFailure = { exception ->
                    if (exception is StaleDataException) {
                        _uiState.update {
                            it.copy(
                                isShowingStaleData = true,
                                isSearchLoadingMore = false,
                                isRefreshing = false,
                            )
                        }
                        return@fold
                    }
                    _uiState.update {
                        it.copy(
                            screenState =
                                if (isInitialLoad && it.searchMovieResults.isEmpty()) {
                                    SearchScreenState.Error(exception.message ?: "Unknown error")
                                } else {
                                    it.screenState
                                },
                            isSearchLoadingMore = false,
                            isRefreshing = false,
                            isShowingStaleData = false,
                            isSearchEndReached = true,
                        )
                    }
                },
            )
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
                viewModelScope.launch { saveRecentSearchUseCase(query) }
            }
        }

        private fun onMovieClicked(movieId: Int) {
            val query = _uiState.value.query
            if (query.isNotBlank()) {
                viewModelScope.launch { saveRecentSearchUseCase(query) }
            }
            _effect.trySend(SearchMoviesEffect.NavigateToDetail(movieId))
        }

        private fun onFieldFocused() {
            _uiState.update { it.copy(isFieldFocused = true) }
        }

        private fun onFieldCleared() {
            _uiState.update { it.copy(isFieldFocused = false) }
        }

        private fun onSuggestionTapped(query: String) {
            _uiState.update { it.copy(query = query, isFieldFocused = false) }
            savedStateHandle[SEARCH_QUERY_KEY] = query
            viewModelScope.launch { saveRecentSearchUseCase(query) }
        }

        private fun onClearRecentSearches() {
            viewModelScope.launch { clearRecentSearchesUseCase() }
        }

        private fun fetchDashboardData(fromRefresh: Boolean = false) {
            if (!fromRefresh) {
                _uiState.update { it.copy(screenState = SearchScreenState.Loading) }
            }

            viewModelScope.launch {
                if (!fromRefresh) cachingRepository.clearStaleEntries()

                val popularDeferred =
                    async { collectWithStale(getPopularMoviesUseCase(FIRST_PAGE)) }
                val nowPlayingDeferred =
                    async { collectWithStale(getNowPlayingMoviesUseCase(FIRST_PAGE)) }
                val topRatedDeferred =
                    async { collectWithStale(getTopRatedMoviesUseCase(FIRST_PAGE)) }
                val upcomingDeferred =
                    async { collectWithStale(getUpcomingMoviesUseCase(FIRST_PAGE)) }
                val trendingDeferred =
                    async { collectWithStale(getTrendingMoviesUseCase(FIRST_PAGE)) }

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

                _uiState.update { state ->
                    val nowPlayingList = nowPlayingResult.getOrDefault(emptyList()).toUiList()
                    val popularList = popularResult.getOrDefault(emptyList()).toUiList()

                    state.copy(
                        screenState = SearchScreenState.Content,
                        isShowingStaleData = isStale,
                        isRefreshing = false,
                        nowPlayingMovies = nowPlayingList,
                        popularMovies = popularList,
                        topRatedMovies = topRatedResult.getOrDefault(emptyList()).toUiList(),
                        upcomingMovies = upcomingResult.getOrDefault(emptyList()).toUiList(),
                        trendingMovies = trendingResult.getOrDefault(emptyList()).toUiList(),
                        trendingSuggestions =
                            trendingResult
                                .getOrDefault(emptyList())
                                .mapNotNull { movie -> movie.title.takeIf { it.isNotBlank() } }
                                .take(TRENDING_SUGGESTIONS_LIMIT),
                        currentPopularPage =
                            if (popularList.isNotEmpty()) {
                                FIRST_PAGE + NEXT_PAGE
                            } else {
                                FIRST_PAGE
                            },
                        isPopularEndReached = popularList.isEmpty(),
                    )
                }
                if (fromRefresh) _effect.trySend(SearchMoviesEffect.ShowRefreshSuccess)
            }
        }

        private fun getPopularMovies() {
            _uiState.update { it.copy(isPopularLoadingMore = true) }

            viewModelScope.launch {
                getPopularMoviesUseCase(_uiState.value.currentPopularPage).collect { result ->
                    result.fold(
                        onSuccess = { movies ->
                            val newMovies = movies.toUiList()
                            _uiState.update {
                                it.copy(
                                    isPopularLoadingMore = false,
                                    popularMovies = it.popularMovies + newMovies,
                                    currentPopularPage = it.currentPopularPage + NEXT_PAGE,
                                    isPopularEndReached = newMovies.isEmpty(),
                                )
                            }
                        },
                        onFailure = {
                            _uiState.update {
                                it.copy(isPopularLoadingMore = false, isPopularEndReached = true)
                            }
                        },
                    )
                }
            }
        }
    }

private suspend fun collectWithStale(
    flow: kotlinx.coroutines.flow.Flow<Result<List<Movie>>>,
): Pair<Result<List<Movie>>, Boolean> {
    var data: Result<List<Movie>> = Result.failure(Exception("No data"))
    var stale = false
    flow.collect { result ->
        when {
            result.isSuccess -> data = result
            result.isFailure && result.exceptionOrNull() is StaleDataException -> stale = true
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
