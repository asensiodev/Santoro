package com.asensiodev.feature.watchlist.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.watchlist.impl.domain.usecase.GetWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.presentation.mapper.toUiList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class WatchlistMoviesViewModel
    @Inject
    constructor(
        private val getWatchlistMoviesUseCase: GetWatchlistMoviesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(WatchlistMoviesUiState())
        val uiState: StateFlow<WatchlistMoviesUiState> = _uiState.asStateFlow()

        init {
            fetchMovies()
        }

        private fun fetchMovies() {
            // TODO(): revisar si este loading es necesario o redundante
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            viewModelScope.launch {
                getWatchlistMoviesUseCase()
                    .collect { result ->
                        when (result) {
                            is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                            is Result.Success ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        movies = result.data.toUiList(),
                                        hasResults = result.data.isNotEmpty(),
                                        errorMessage = null,
                                    )
                                }

                            is Result.Error ->
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
