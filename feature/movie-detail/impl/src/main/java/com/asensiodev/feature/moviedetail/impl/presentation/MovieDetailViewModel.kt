package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.moviedetail.impl.domain.usecase.GetMovieDetailUseCase
import com.asensiodev.feature.moviedetail.impl.domain.usecase.UpdateMovieStateUseCase
import com.asensiodev.feature.moviedetail.impl.presentation.mapper.toDomain
import com.asensiodev.feature.moviedetail.impl.presentation.mapper.toUi
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MovieDetailViewModel
    @Inject
    constructor(
        private val getMovieDetailUseCase: GetMovieDetailUseCase,
        private val updateMovieStateUseCase: UpdateMovieStateUseCase,
        private val syncScheduler: WorkManagerSyncScheduler,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MovieDetailUiState())
        val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

        private val _effect = Channel<MovieDetailEffect>(Channel.BUFFERED)
        val effect = _effect.receiveAsFlow()

        private var lastRequestedMovieId: Int? = null

        fun process(intent: MovieDetailIntent) {
            when (intent) {
                is MovieDetailIntent.FetchDetails -> fetchMovieDetails(intent.movieId)
                is MovieDetailIntent.ToggleWatched -> toggleWatched()
                is MovieDetailIntent.ToggleWatchlist -> toggleWatchlist()
                is MovieDetailIntent.ShareMovie -> emitShareEffect()
                is MovieDetailIntent.Retry -> retryFetch()
            }
        }

        private fun fetchMovieDetails(movieId: Int) {
            lastRequestedMovieId = movieId
            showLoading()
            viewModelScope.launch {
                getMovieDetailUseCase(movieId)
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        movie = result.data?.toUi(),
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

        private fun retryFetch() {
            val movieId = lastRequestedMovieId ?: return
            fetchMovieDetails(movieId)
        }

        private fun emitShareEffect() {
            val movie = _uiState.value.movie ?: return
            viewModelScope.launch {
                _effect.send(MovieDetailEffect.ShareMovie(movie))
            }
        }

        private fun toggleWatchlist() {
            val movie = uiState.value.movie ?: return
            val updatedMovie = movie.copy(isInWatchlist = !movie.isInWatchlist)
            viewModelScope.launch {
                when (val result = updateMovieStateUseCase(updatedMovie.toDomain())) {
                    is Result.Success -> {
                        _uiState.update { it.copy(movie = updatedMovie) }
                        syncScheduler.enqueueUpload(movie.id)
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(errorMessage = result.exception.message) }
                    }
                }
            }
        }

        private fun toggleWatched() {
            val movie = uiState.value.movie ?: return
            val isNowWatched = !movie.isWatched
            val updatedMovie =
                movie.copy(
                    isWatched = isNowWatched,
                    watchedAt = if (isNowWatched) System.currentTimeMillis() else null,
                )
            viewModelScope.launch {
                when (val result = updateMovieStateUseCase(updatedMovie.toDomain())) {
                    is Result.Success -> {
                        _uiState.update { it.copy(movie = updatedMovie) }
                        syncScheduler.enqueueUpload(movie.id)
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(errorMessage = result.exception.message) }
                    }
                }
            }
        }

        private fun showLoading() {
            _uiState.update { it.copy(isLoading = true) }
        }
    }
