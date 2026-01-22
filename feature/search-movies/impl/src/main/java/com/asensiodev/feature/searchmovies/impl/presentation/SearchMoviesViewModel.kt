package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetNowPlayingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetPopularMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTopRatedMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetUpcomingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.presentation.mapper.toUiList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    ) : ViewModel() {
        private var searchJob: Job? = null
        private val _uiState = MutableStateFlow(SearchMoviesUiState())
        val uiState: StateFlow<SearchMoviesUiState> = _uiState.asStateFlow()

        private val searchQuery =
            savedStateHandle.getStateFlow(SEARCH_QUERY_KEY, "")

        @OptIn(FlowPreview::class)
        fun loadInitialData() {
            fetchDashboardData()

            searchQuery
                .debounce(DELAY)
                .distinctUntilChanged()
                .onEach { query ->
                    handleQueryChange(query)
                }.launchIn(viewModelScope)
        }

        private fun handleQueryChange(query: String) {
            if (query.isBlank()) {
                _uiState.update {
                    it.copy(
                        query = query,
                        screenState = SearchScreenState.Content,
                        searchMovieResults = emptyList(),
                        currentSearchPage = FIRST_PAGE,
                        isSearchEndReached = false,
                    )
                }
            } else {
                val currentUiQuery = _uiState.value.query
                val isNewQuery = currentUiQuery != query
                _uiState.update {
                    it.copy(
                        query = query,
                        searchMovieResults = if (isNewQuery) emptyList() else it.searchMovieResults,
                        currentSearchPage = if (isNewQuery) FIRST_PAGE else it.currentSearchPage,
                        isSearchEndReached = if (isNewQuery) false else it.isSearchEndReached,
                    )
                }
                fetchSearchMoviesResult(
                    query,
                    FIRST_PAGE,
                    isInitialLoad = isNewQuery,
                )
            }
        }

        fun updateQuery(query: String) {
            savedStateHandle[SEARCH_QUERY_KEY] = query
        }

        private fun fetchSearchMoviesResult(
            query: String,
            page: Int,
            isInitialLoad: Boolean = false,
        ) {
            if (isInitialLoad) searchJob?.cancel()

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
                    searchMoviesUseCase(query, page).collect { result ->
                        handleSearchResult(result, isInitialLoad, page)
                    }
                }
        }

        private fun handleSearchResult(
            result: Result<List<Movie>>,
            isInitialLoad: Boolean,
            page: Int,
        ) {
            when (result) {
                is Result.Success -> {
                    val newMovies = result.data.toUiList()
                    val updatedResults =
                        if (isInitialLoad) {
                            newMovies
                        } else {
                            _uiState.value.searchMovieResults + newMovies
                        }
                    _uiState.update {
                        it.copy(
                            screenState = SearchScreenState.Content,
                            isSearchLoadingMore = false,
                            searchMovieResults = updatedResults,
                            currentSearchPage = page,
                            isSearchEndReached = newMovies.isEmpty(),
                        )
                    }
                }

                is Result.Error -> {
                    val exception = result.exception
                    _uiState.update {
                        it.copy(
                            screenState =
                                if (isInitialLoad && it.searchMovieResults.isEmpty()) {
                                    SearchScreenState.Error(exception.message ?: "Unknown error")
                                } else {
                                    it.screenState
                                },
                            isSearchLoadingMore = false,
                            isSearchEndReached = true,
                        )
                    }
                }
            }
        }

        fun loadMorePopularMovies() {
            if (!_uiState.value.isPopularLoadingMore && !_uiState.value.isPopularEndReached) {
                getPopularMovies()
            }
        }

        fun loadMoreSearchResults() {
            val currentPage = _uiState.value.currentSearchPage
            val query = _uiState.value.query
            if (!_uiState.value.isSearchLoadingMore &&
                !_uiState.value.isSearchEndReached &&
                query.isNotBlank()
            ) {
                fetchSearchMoviesResult(query, currentPage + NEXT_PAGE)
            }
        }

        private fun fetchDashboardData() {
            _uiState.update { it.copy(screenState = SearchScreenState.Loading) }

            viewModelScope.launch {
                val popularDeferred = async { getPopularMoviesUseCase(FIRST_PAGE).first() }
                val nowPlayingDeferred = async { getNowPlayingMoviesUseCase(FIRST_PAGE).first() }
                val topRatedDeferred = async { getTopRatedMoviesUseCase(FIRST_PAGE).first() }
                val upcomingDeferred = async { getUpcomingMoviesUseCase(FIRST_PAGE).first() }

                val results =
                    DashboardResults(
                        popular = popularDeferred.await(),
                        nowPlaying = nowPlayingDeferred.await(),
                        topRated = topRatedDeferred.await(),
                        upcoming = upcomingDeferred.await(),
                    )

                _uiState.update { state ->
                    val popularList = results.popular.getOrDefault(emptyList()).toUiList()
                    val nowPlayingList = results.nowPlaying.getOrDefault(emptyList()).toUiList()
                    val topRatedList = results.topRated.getOrDefault(emptyList()).toUiList()
                    val upcomingList = results.upcoming.getOrDefault(emptyList()).toUiList()

                    state.copy(
                        screenState = SearchScreenState.Content,
                        popularMovies = popularList,
                        nowPlayingMovies = nowPlayingList,
                        topRatedMovies = topRatedList,
                        upcomingMovies = upcomingList,
                        currentPopularPage =
                            if (popularList.isNotEmpty()) {
                                FIRST_PAGE +
                                    NEXT_PAGE
                            } else {
                                FIRST_PAGE
                            },
                        isPopularEndReached = popularList.isEmpty(),
                    )
                }
            }
        }

        private fun <T> Result<T>.getOrDefault(defaultValue: T): T =
            if (this is Result.Success) this.data else defaultValue

        private fun getPopularMovies() {
            _uiState.update { it.copy(isPopularLoadingMore = true) }

            viewModelScope.launch {
                getPopularMoviesUseCase(_uiState.value.currentPopularPage).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val newMovies = result.data.toUiList()
                            _uiState.update {
                                it.copy(
                                    isPopularLoadingMore = false,
                                    popularMovies = it.popularMovies + newMovies,
                                    currentPopularPage = it.currentPopularPage + NEXT_PAGE,
                                    isPopularEndReached = newMovies.isEmpty(),
                                )
                            }
                        }

                        is Result.Error -> {
                            _uiState.update {
                                it.copy(isPopularLoadingMore = false, isPopularEndReached = true)
                            }
                        }
                    }
                }
            }
        }

        private data class DashboardResults(
            val popular: Result<List<Movie>>,
            val nowPlaying: Result<List<Movie>>,
            val topRated: Result<List<Movie>>,
            val upcoming: Result<List<Movie>>,
        )
    }

private const val DELAY: Long = 500
private const val FIRST_PAGE: Int = 1
private const val NEXT_PAGE: Int = 1
private const val SEARCH_QUERY_KEY = "search_query"
