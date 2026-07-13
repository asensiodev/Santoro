package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.usecase.ObserveHasSeenDetailTooltipUseCase
import com.asensiodev.core.domain.usecase.SetDetailTooltipSeenUseCase
import com.asensiodev.feature.moviedetail.impl.domain.usecase.GetMovieDetailUseCase
import com.asensiodev.feature.moviedetail.impl.domain.usecase.UpdateMovieStateUseCase
import com.asensiodev.feature.moviedetail.impl.presentation.mapper.toDomain
import com.asensiodev.feature.moviedetail.impl.presentation.mapper.toUi
import com.asensiodev.library.observability.api.NoOpObservabilityTracker
import com.asensiodev.library.observability.api.ObservabilityTracker
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
        private val observeHasSeenDetailTooltipUseCase: ObserveHasSeenDetailTooltipUseCase,
        private val setDetailTooltipSeenUseCase: SetDetailTooltipSeenUseCase,
        private val observabilityTracker: ObservabilityTracker = NoOpObservabilityTracker,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MovieDetailUiState())
        val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

        private val _effect = Channel<MovieDetailEffect>(Channel.BUFFERED)
        val effect = _effect.receiveAsFlow()

        private var lastRequestedMovieId: Int? = null
        private var fetchJob: Job? = null

        fun process(intent: MovieDetailIntent) {
            when (intent) {
                is MovieDetailIntent.FetchDetails -> fetchMovieDetails(intent.movieId)
                is MovieDetailIntent.ToggleWatched -> toggleWatched()
                is MovieDetailIntent.ToggleWatchlist -> toggleWatchlist()
                is MovieDetailIntent.ShareMovie -> emitShareEffect()
                is MovieDetailIntent.Retry -> retryFetch()
                is MovieDetailIntent.DismissTooltip -> dismissTooltip()
            }
        }

        private fun fetchMovieDetails(movieId: Int) {
            lastRequestedMovieId = movieId
            observabilityTracker.trackScreen(SCREEN_MOVIE_DETAIL)
            observabilityTracker.trackAction(
                MOVIE_DETAIL_FETCH,
                mapOf(
                    MOVIE_ID to movieId.toString(),
                ),
            )
            _uiState.update { it.copy(screenState = MovieDetailScreenState.Loading) }
            fetchJob?.cancel()
            fetchJob =
                viewModelScope.launch {
                    getMovieDetailUseCase(movieId)
                        .collect { result ->
                            result.fold(
                                onSuccess = { movie ->
                                    _uiState.update {
                                        it.copy(
                                            screenState = MovieDetailScreenState.Content,
                                            movie = movie?.toUi(),
                                        )
                                    }
                                    checkTooltip()
                                },
                                onFailure = { exception ->
                                    observabilityTracker.recordError(
                                        MOVIE_DETAIL_FETCH_FAILED,
                                        exception,
                                        mapOf(MOVIE_ID to movieId.toString()),
                                    )
                                    _uiState.update {
                                        it.copy(
                                            screenState =
                                                MovieDetailScreenState.Error(
                                                    exception.message.orEmpty(),
                                                ),
                                        )
                                    }
                                },
                            )
                        }
                }
        }

        private fun retryFetch() {
            val movieId = lastRequestedMovieId ?: return
            fetchMovieDetails(movieId)
        }

        private fun emitShareEffect() {
            val movie = _uiState.value.movie ?: return
            observabilityTracker.trackAction(
                MOVIE_DETAIL_SHARE,
                mapOf(
                    MOVIE_ID to movie.id.toString(),
                ),
            )
            viewModelScope.launch {
                _effect.send(MovieDetailEffect.ShareMovie(movie))
            }
        }

        private fun toggleWatchlist() {
            val state = uiState.value
            val movie = state.movie ?: return
            if (state.isMovieStateUpdatePending) return
            val isNowInWatchlist = !movie.isInWatchlist
            val updatedMovie =
                movie.copy(
                    isInWatchlist = isNowInWatchlist,
                    isWatched = !isNowInWatchlist && movie.isWatched,
                    watchedAt = if (isNowInWatchlist) null else movie.watchedAt,
                )
            _uiState.update { it.copy(isMovieStateUpdatePending = true) }
            viewModelScope.launch {
                try {
                    updateMovieStateUseCase(updatedMovie.toDomain())
                        .onSuccess {
                            observabilityTracker.trackAction(
                                MOVIE_DETAIL_TOGGLE_WATCHLIST,
                                mapOf(
                                    MOVIE_ID to movie.id.toString(),
                                    IS_ENABLED to isNowInWatchlist.toString(),
                                ),
                            )
                            _uiState.update { it.copy(movie = updatedMovie) }
                            runCatching { syncScheduler.enqueueUpload(movie.id) }
                        }.onFailure { exception ->
                            observabilityTracker.recordError(
                                MOVIE_DETAIL_TOGGLE_WATCHLIST_FAILED,
                                exception,
                                mapOf(MOVIE_ID to movie.id.toString()),
                            )
                            _effect
                                .trySend(
                                    MovieDetailEffect.ShowError(
                                        exception.message.orEmpty(),
                                    ),
                                )
                        }
                } finally {
                    _uiState.update { it.copy(isMovieStateUpdatePending = false) }
                }
            }
        }

        private fun toggleWatched() {
            val state = uiState.value
            val movie = state.movie ?: return
            if (state.isMovieStateUpdatePending) return
            val isNowWatched = !movie.isWatched
            val updatedMovie =
                movie.copy(
                    isWatched = isNowWatched,
                    isInWatchlist = !isNowWatched && movie.isInWatchlist,
                    watchedAt = if (isNowWatched) System.currentTimeMillis() else null,
                )
            _uiState.update { it.copy(isMovieStateUpdatePending = true) }
            viewModelScope.launch {
                try {
                    updateMovieStateUseCase(updatedMovie.toDomain())
                        .onSuccess {
                            observabilityTracker.trackAction(
                                MOVIE_DETAIL_TOGGLE_WATCHED,
                                mapOf(
                                    MOVIE_ID to movie.id.toString(),
                                    IS_ENABLED to isNowWatched.toString(),
                                ),
                            )
                            _uiState.update { it.copy(movie = updatedMovie) }
                            runCatching { syncScheduler.enqueueUpload(movie.id) }
                        }.onFailure { exception ->
                            observabilityTracker.recordError(
                                MOVIE_DETAIL_TOGGLE_WATCHED_FAILED,
                                exception,
                                mapOf(MOVIE_ID to movie.id.toString()),
                            )
                            _effect
                                .trySend(
                                    MovieDetailEffect.ShowError(
                                        exception.message.orEmpty(),
                                    ),
                                )
                        }
                } finally {
                    _uiState.update { it.copy(isMovieStateUpdatePending = false) }
                }
            }
        }

        private fun checkTooltip() {
            viewModelScope.launch {
                val hasSeen = observeHasSeenDetailTooltipUseCase().first()
                if (!hasSeen) {
                    _uiState.update { it.copy(showTooltip = true) }
                }
            }
        }

        private fun dismissTooltip() {
            observabilityTracker.trackAction(MOVIE_DETAIL_TOOLTIP_DISMISSED)
            _uiState.update { it.copy(showTooltip = false) }
            viewModelScope.launch {
                setDetailTooltipSeenUseCase()
            }
        }

        private companion object {
            const val SCREEN_MOVIE_DETAIL = "movie_detail"
            const val MOVIE_ID = "movie_id"
            const val IS_ENABLED = "is_enabled"
            const val MOVIE_DETAIL_FETCH = "movie_detail_fetch"
            const val MOVIE_DETAIL_FETCH_FAILED = "movie_detail_fetch_failed"
            const val MOVIE_DETAIL_SHARE = "movie_detail_share"
            const val MOVIE_DETAIL_TOGGLE_WATCHLIST = "movie_detail_toggle_watchlist"
            const val MOVIE_DETAIL_TOGGLE_WATCHLIST_FAILED = "movie_detail_toggle_watchlist_failed"
            const val MOVIE_DETAIL_TOGGLE_WATCHED = "movie_detail_toggle_watched"
            const val MOVIE_DETAIL_TOGGLE_WATCHED_FAILED = "movie_detail_toggle_watched_failed"
            const val MOVIE_DETAIL_TOOLTIP_DISMISSED = "movie_detail_tooltip_dismissed"
        }
    }
