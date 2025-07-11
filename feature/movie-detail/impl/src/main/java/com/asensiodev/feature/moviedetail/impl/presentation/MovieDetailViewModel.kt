package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.moviedetail.impl.domain.usecase.GetMovieDetailUseCase
import com.asensiodev.feature.moviedetail.impl.domain.usecase.UpdateMovieStateUseCase
import com.asensiodev.feature.moviedetail.impl.presentation.mapper.toDomain
import com.asensiodev.feature.moviedetail.impl.presentation.mapper.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MovieDetailViewModel
    @Inject
    constructor(
        private val getMovieDetailUseCase: GetMovieDetailUseCase,
        private val updateMovieStateUseCase: UpdateMovieStateUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MovieDetailUiState())
        val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

        fun fetchMovieDetails(movieId: Int) {
            viewModelScope.launch {
                getMovieDetailUseCase(movieId)
                    .collect { result ->
                        when (result) {
                            is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                            is Result.Success ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        movie = result.data?.toUi(),
                                        errorMessage = null,
                                    )
                                }

                            is Result.Error ->
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

        fun toggleWatchlist() {
            val movie = uiState.value.movie
            if (movie != null) {
                val updatedMovie = movie.copy(isInWatchlist = !movie.isInWatchlist)
                viewModelScope.launch {
                    when (val result = updateMovieStateUseCase(updatedMovie.toDomain())) {
                        is Result.Success -> {
                            _uiState.update { it.copy(movie = updatedMovie) }
                        }

                        is Result.Error -> {
                            _uiState.update {
                                it.copy(errorMessage = result.exception.message)
                            }
                        }

                        Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            }
        }

        fun toggleWatched() {
            val movie = uiState.value.movie
            if (movie != null) {
                val updatedMovie = movie.copy(isWatched = !movie.isWatched)
                viewModelScope.launch {
                    when (val result = updateMovieStateUseCase(updatedMovie.toDomain())) {
                        is Result.Success -> {
                            _uiState.update { it.copy(movie = updatedMovie) }
                        }

                        is Result.Error -> {
                            _uiState.update {
                                it.copy(errorMessage = result.exception.message)
                            }
                        }

                        Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            }
        }
    }
