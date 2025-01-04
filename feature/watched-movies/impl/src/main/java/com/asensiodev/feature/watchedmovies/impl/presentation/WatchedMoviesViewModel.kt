package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.presentation.mapper.toUiList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(WatchedMoviesUiState())
        val uiState: StateFlow<WatchedMoviesUiState> = _uiState.asStateFlow()

        init {
            fetchMovies()
        }

        private fun fetchMovies() {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            viewModelScope.launch {
                getWatchedMoviesUseCase()
                    .onEach { result ->
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
                    }.catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message,
                                hasResults = false,
                            )
                        }
                    }.launchIn(viewModelScope)
            }
        }
    }
