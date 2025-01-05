package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.SearchWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.presentation.mapper.toUiList
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
internal class WatchedMoviesViewModel
    @Inject
    constructor(
        private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase,
        private val searchWatchedMoviesUseCase: SearchWatchedMoviesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(WatchedMoviesUiState())
        val uiState: StateFlow<WatchedMoviesUiState> = _uiState.asStateFlow()

        private val searchQuery = MutableStateFlow("")

        init {
            fetchWatchedMovies()

            searchQuery
                .debounce(DELAY)
                .distinctUntilChanged()
                .onEach { query ->
                    if (query.isBlank()) {
                        fetchWatchedMovies()
                    } else {
                        searchWatchedMovies(query)
                    }
                }.launchIn(viewModelScope)
        }

        private fun fetchWatchedMovies() {
            viewModelScope.launch {
                getWatchedMoviesUseCase()
                    .collect { result ->
                        when (result) {
                            is Result.Loading -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = true,
                                    )
                                }
                            }

                            is Result.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        movies = result.data.toUiList(),
                                        hasResults = result.data.isNotEmpty(),
                                        errorMessage = null,
                                    )
                                }
                            }

                            is Result.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = result.exception.message,
                                        hasResults = false,
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

        private fun searchWatchedMovies(query: String) {
            viewModelScope.launch {
                searchWatchedMoviesUseCase(query).collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }

                        is Result.Success -> {
                            val moviesUi = result.data.toUiList()
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    movies = moviesUi,
                                    hasResults = moviesUi.isNotEmpty(),
                                    errorMessage = null,
                                )
                            }
                        }

                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = result.exception.message,
                                    hasResults = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

private const val DELAY: Long = 500
