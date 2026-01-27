package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.getOrDefault
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetMoviesByGenreUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetNowPlayingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetPopularMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTopRatedMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTrendingMoviesUseCase
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
        private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
        private val getMoviesByGenreUseCase: GetMoviesByGenreUseCase,
    ) : ViewModel() {
        private var searchJob: Job? = null
        private val _uiState = MutableStateFlow(SearchMoviesUiState())
        val uiState: StateFlow<SearchMoviesUiState> = _uiState.asStateFlow()

        private val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY_KEY, "")

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

        fun updateQuery(query: String) {
            _uiState.update { it.copy(query = query) }

            if (query.isNotBlank() && _uiState.value.selectedGenreId != null) {
                clearGenreSelection()
            }

            savedStateHandle[SEARCH_QUERY_KEY] = query
        }

        fun onGenreSelected(genreId: Int) {
            if (_uiState.value.query.isNotBlank()) {
                updateQuery("")
            }

            _uiState.update {
                it.copy(
                    selectedGenreId = genreId,
                    searchMovieResults = emptyList(),
                    currentSearchPage = FIRST_PAGE,
                    isSearchEndReached = false,
                )
            }
            fetchMoviesByGenre(genreId, FIRST_PAGE, isInitialLoad = true)
        }

        fun clearGenreSelection() {
            _uiState.update {
                it.copy(
                    selectedGenreId = null,
                    searchMovieResults = emptyList(),
                    screenState = SearchScreenState.Content,
                )
            }
        }

        private fun handleQueryChange(query: String) {
            if (query.isBlank()) {
                _uiState.update {
                    it.copy(
                        screenState = SearchScreenState.Content,
                        searchMovieResults =
                            if (it.selectedGenreId == null) {
                                emptyList()
                            } else {
                                it.searchMovieResults
                            },
                        currentSearchPage = FIRST_PAGE,
                        isSearchEndReached = false,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        searchMovieResults = emptyList(),
                        currentSearchPage = FIRST_PAGE,
                        isSearchEndReached = false,
                    )
                }
                fetchSearchMoviesResult(
                    query,
                    FIRST_PAGE,
                    isInitialLoad = true,
                )
            }
        }

        private fun fetchMoviesByGenre(
            genreId: Int,
            page: Int,
            isInitialLoad: Boolean,
        ) {
            if (isInitialLoad) searchJob?.cancel()

            _uiState.update {
                it.copy(
                    screenState =
                        if (isInitialLoad &&
                            it.searchMovieResults.isEmpty()
                        ) {
                            SearchScreenState.Loading
                        } else {
                            it.screenState
                        },
                    isSearchLoadingMore = !isInitialLoad,
                )
            }

            searchJob =
                viewModelScope.launch {
                    getMoviesByGenreUseCase(genreId, page).collect { result ->
                        handleSearchResult(result, isInitialLoad, page)
                    }
                }
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
            val selectedGenreId = _uiState.value.selectedGenreId

            if (!_uiState.value.isSearchLoadingMore && !_uiState.value.isSearchEndReached) {
                if (query.isNotBlank()) {
                    fetchSearchMoviesResult(query, currentPage + NEXT_PAGE)
                } else if (selectedGenreId != null) {
                    fetchMoviesByGenre(
                        selectedGenreId,
                        currentPage + NEXT_PAGE,
                        isInitialLoad = false,
                    )
                }
            }
        }

        private fun fetchDashboardData() {
            _uiState.update { it.copy(screenState = SearchScreenState.Loading) }

            viewModelScope.launch {
                val results =
                    DashboardResults(
                        popular = async { getPopularMoviesUseCase(FIRST_PAGE).first() }.await(),
                        nowPlaying =
                            async {
                                getNowPlayingMoviesUseCase(
                                    FIRST_PAGE,
                                ).first()
                            }.await(),
                        topRated =
                            async {
                                getTopRatedMoviesUseCase(
                                    FIRST_PAGE,
                                ).first()
                            }.await(),
                        upcoming =
                            async {
                                getUpcomingMoviesUseCase(
                                    FIRST_PAGE,
                                ).first()
                            }.await(),
                        trending =
                            async {
                                getTrendingMoviesUseCase(
                                    FIRST_PAGE,
                                ).first()
                            }.await(),
                    )

                _uiState.update { state ->
                    val nowPlayingList = results.nowPlaying.getOrDefault(emptyList()).toUiList()
                    val popularList = results.popular.getOrDefault(emptyList()).toUiList()

                    state.copy(
                        screenState = SearchScreenState.Content,
                        nowPlayingMovies = nowPlayingList,
                        popularMovies = popularList,
                        topRatedMovies = results.topRated.getOrDefault(emptyList()).toUiList(),
                        upcomingMovies = results.upcoming.getOrDefault(emptyList()).toUiList(),
                        trendingMovies = results.trending.getOrDefault(emptyList()).toUiList(),
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
            val trending: Result<List<Movie>>,
        )
    }

private const val DELAY: Long = 500
private const val FIRST_PAGE: Int = 1
private const val NEXT_PAGE: Int = 1
private const val SEARCH_QUERY_KEY = "search_query"
