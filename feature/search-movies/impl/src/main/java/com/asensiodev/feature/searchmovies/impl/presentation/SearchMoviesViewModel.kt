package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetPopularMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.presentation.mapper.toUiList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SearchMoviesViewModel
    @Inject
    constructor(
        private val searchMoviesUseCase: SearchMoviesUseCase,
        private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SearchMoviesUiState())
        val uiState: StateFlow<SearchMoviesUiState> = _uiState.asStateFlow()

        private val searchQuery = MutableStateFlow("")

        init {
            fetchPopularMovies(isInitialLoad = true)

            searchQuery
                .debounce(DELAY)
                .distinctUntilChanged()
                .onEach { query ->
                    if (query.isBlank()) {
                        _uiState.update {
                            it.copy(
                                searchMovieResults = emptyList(),
                                hasSearchResults = false,
                                currentSearchPage = FIRST_PAGE,
                                isSearchEndReached = false,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                searchMovieResults = emptyList(),
                                hasSearchResults = false,
                                currentSearchPage = FIRST_PAGE,
                                isSearchEndReached = false,
                            )
                        }
                        fetchSearchMoviesResult(query, FIRST_PAGE, isInitialLoad = true)
                    }
                }.launchIn(viewModelScope)
        }

        private fun fetchPopularMovies(isInitialLoad: Boolean = false) {
            viewModelScope.launch {
                getPopularMoviesUseCase(_uiState.value.currentPopularPage)
                    .collect { result ->
                        when (result) {
                            is Result.Loading -> {
                                _uiState.update {
                                    it.copy(
                                        isPopularMoviesLoading = true,
                                        isInitialLoading = isInitialLoad,
                                    )
                                }
                            }

                            is Result.Success -> {
                                val newMovies = result.data.toUiList()
                                val updatedPopularMovies = _uiState.value.popularMovies + newMovies
                                _uiState.update {
                                    it.copy(
                                        isPopularMoviesLoading = false,
                                        isInitialLoading = false,
                                        popularMovies = updatedPopularMovies,
                                        currentPopularPage = it.currentPopularPage + NEXT_PAGE,
                                        isPopularEndReached = newMovies.isEmpty(),
                                        hasPopularMoviesResults = updatedPopularMovies.isNotEmpty(),
                                        errorMessage = null,
                                    )
                                }
                            }

                            is Result.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isPopularMoviesLoading = false,
                                        isInitialLoading = false,
                                        errorMessage = result.exception.message,
                                        hasPopularMoviesResults = false,
                                        isPopularEndReached = true,
                                    )
                                }
                            }
                        }
                    }
            }
        }

        fun updateQuery(query: String) {
            searchQuery.value = query
            _uiState.update { it.copy(query = query) }
        }

        private fun fetchSearchMoviesResult(
            query: String,
            page: Int,
            isInitialLoad: Boolean = false,
        ) {
            viewModelScope.launch {
                searchMoviesUseCase(query, page)
                    .collect { result ->
                        when (result) {
                            is Result.Loading -> {
                                _uiState.update {
                                    it.copy(
                                        isSearchLoading = true,
                                        isInitialLoading = isInitialLoad,
                                    )
                                }
                            }

                            is Result.Success -> {
                                val newMovies = result.data.toUiList()
                                val updatedSearchResults =
                                    _uiState.value.searchMovieResults + newMovies
                                _uiState.update {
                                    it.copy(
                                        isSearchLoading = false,
                                        isInitialLoading = false,
                                        searchMovieResults = updatedSearchResults,
                                        hasSearchResults = updatedSearchResults.isNotEmpty(),
                                        currentSearchPage = page,
                                        isSearchEndReached = newMovies.isEmpty(),
                                        errorMessage = null,
                                    )
                                }
                            }

                            is Result.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isSearchLoading = false,
                                        isInitialLoading = false,
                                        errorMessage = result.exception.message,
                                        hasSearchResults = false,
                                        isSearchEndReached = true,
                                    )
                                }
                            }
                        }
                    }
            }
        }

        fun loadMorePopularMovies() {
            if (!_uiState.value.isPopularMoviesLoading && !_uiState.value.isPopularEndReached) {
                fetchPopularMovies()
            }
        }

        fun loadMoreSearchResults() {
            val currentPage = _uiState.value.currentSearchPage
            val query = _uiState.value.query
            if (!_uiState.value.isSearchLoading &&
                !_uiState.value.isSearchEndReached &&
                query.isNotBlank()
            ) {
                fetchSearchMoviesResult(query, currentPage + NEXT_PAGE)
            }
        }
    }

private const val DELAY: Long = 500
private const val FIRST_PAGE: Int = 1
private const val NEXT_PAGE: Int = 1
