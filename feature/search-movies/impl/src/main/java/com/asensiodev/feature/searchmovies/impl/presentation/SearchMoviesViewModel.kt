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
            fetchPopularMovies()

            searchQuery
                .debounce(DELAY)
                .distinctUntilChanged()
                .onEach { query ->
                    if (query.isBlank()) {
                        _uiState.update {
                            it.copy(
                                searchMovieResults = emptyList(),
                                hasSearchResults = false,
                            )
                        }
                    } else {
                        fetchMovies(query)
                    }
                }.launchIn(viewModelScope)
        }

        private fun fetchPopularMovies() {
            viewModelScope.launch {
                getPopularMoviesUseCase()
                    .collect { result ->
                        when (result) {
                            is Result.Loading ->
                                _uiState.update {
                                    it.copy(
                                        isPopularMoviesLoading = true,
                                    )
                                }
                            is Result.Success -> {
                                val uiMovies = result.data.toUiList()
                                _uiState.update {
                                    it.copy(
                                        popularMovies = uiMovies,
                                        hasPopularMoviesResults = uiMovies.isNotEmpty(),
                                        isPopularMoviesLoading = false,
                                        errorMessage = null,
                                    )
                                }
                            }

                            is Result.Error ->
                                _uiState.update {
                                    it.copy(
                                        isPopularMoviesLoading = false,
                                        errorMessage = result.exception.message,
                                        hasPopularMoviesResults = false,
                                    )
                                }
                        }
                    }
            }
        }

        fun updateQuery(query: String) {
            searchQuery.value = query
            _uiState.update { it.copy(query = query) }
        }

        private fun fetchMovies(query: String) {
            viewModelScope.launch {
                searchMoviesUseCase(query)
                    .collect { result ->
                        when (result) {
                            is Result.Loading -> _uiState.update { it.copy(isSearchLoading = true) }
                            is Result.Success ->
                                _uiState.update {
                                    it.copy(
                                        isSearchLoading = false,
                                        searchMovieResults = result.data.toUiList(),
                                        hasSearchResults = result.data.isNotEmpty(),
                                        errorMessage = null,
                                    )
                                }

                            is Result.Error ->
                                _uiState.update {
                                    it.copy(
                                        isSearchLoading = false,
                                        errorMessage = result.exception.message,
                                        hasSearchResults = false,
                                    )
                                }
                        }
                    }
            }
        }
    }

private const val DELAY: Long = 500
