package com.asensiodev.feature.watchlist.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.watchlist.impl.domain.usecase.GetWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.SearchWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.presentation.mapper.toUiList
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
internal class WatchlistMoviesViewModel
    @Inject
    constructor(
        private val getWatchlistMoviesUseCase: GetWatchlistMoviesUseCase,
        private val searchWatchlistMoviesUseCase: SearchWatchlistMoviesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(WatchlistMoviesUiState())
        val uiState: StateFlow<WatchlistMoviesUiState> = _uiState.asStateFlow()

        private val searchQuery = MutableStateFlow("")

        init {
            fetchWatchlistMovies()

            searchQuery
                .debounce(DELAY)
                .distinctUntilChanged()
                .onEach { query ->
                    if (query.isBlank()) {
                        fetchWatchlistMovies()
                    } else {
                        searchWatchlistMovies(query)
                    }
                }.launchIn(viewModelScope)
        }

        private fun fetchWatchlistMovies() {
            showLoading()
            viewModelScope.launch {
                getWatchlistMoviesUseCase()
                    .collect { result ->
                        when (result) {
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

        private fun searchWatchlistMovies(query: String) {
            showLoading()
            viewModelScope.launch {
                searchWatchlistMoviesUseCase(query).collect { result ->
                    when (result) {
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

        private fun showLoading() {
            _uiState.update { it.copy(isLoading = true) }
        }
    }

private const val DELAY: Long = 500
