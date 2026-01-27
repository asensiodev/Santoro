package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.SearchWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.presentation.mapper.toUiList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
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

@OptIn(FlowPreview::class)
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
            showLoading()
            viewModelScope.launch {
                getWatchedMoviesUseCase()
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                val movies = result.data.toUiList()
                                val groupedMovies = movies.groupBy { it.watchedDate ?: "Unknown" }
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        movies = groupedMovies,
                                        errorMessage = null,
                                    )
                                }
                            }

                            is Result.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = result.exception.message,
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
            _uiState.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                searchWatchedMoviesUseCase(query).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val moviesUi = result.data.toUiList()
                            val groupedMovies = moviesUi.groupBy { it.watchedDate ?: "Unknown" }
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    movies = groupedMovies,
                                    errorMessage = null,
                                )
                            }
                        }

                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = result.exception.message,
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
