package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.usecase.ObserveHasSeenDetailTooltipUseCase
import com.asensiodev.core.domain.usecase.SetDetailTooltipSeenUseCase
import com.asensiodev.feature.moviedetail.impl.domain.usecase.GetMovieDetailUseCase
import com.asensiodev.feature.moviedetail.impl.domain.usecase.UpdateMovieStateUseCase
import com.asensiodev.feature.moviedetail.impl.presentation.mapper.toDomain
import com.asensiodev.feature.moviedetail.impl.presentation.mapper.toUi
import com.asensiodev.library.observability.api.NoOpObservabilityTracker
import com.asensiodev.library.observability.api.ObservabilityTracker
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import com.asensiodev.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.asensiodev.santoro.core.stringresources.R as SR

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

        private val _effect = MutableSharedFlow<MovieDetailEffect>(extraBufferCapacity = 1)
        val effect = _effect.asSharedFlow()

        private var lastRequestedMovieId: Int? = null
        private var fetchJob: Job? = null
        private var mutationJob: Job? = null
        private var tooltipJob: Job? = null
        private var tooltipCheckedMovieId: Int? = null
        private var tooltipJobRequestVersion: Int? = null
        private var activeRequestVersion = 0

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
            activeRequestVersion++
            val requestVersion = activeRequestVersion
            lastRequestedMovieId = movieId
            fetchJob?.cancel()
            mutationJob?.cancel()
            tooltipJob?.cancel()
            observabilityTracker.trackScreen(SCREEN_MOVIE_DETAIL)
            observabilityTracker.trackAction(
                MOVIE_DETAIL_FETCH,
                mapOf(
                    MOVIE_ID to movieId.toString(),
                ),
            )
            _uiState.value = MovieDetailUiState()
            fetchJob =
                viewModelScope.launch {
                    getMovieDetailUseCase(movieId)
                        .collect { result ->
                            if (requestVersion != activeRequestVersion) return@collect
                            handleMovieDetailResult(result, movieId, requestVersion)
                        }
                }
        }

        private fun handleMovieDetailResult(
            result: Result<Movie?>,
            movieId: Int,
            requestVersion: Int,
        ) {
            result.fold(
                onSuccess = { movie ->
                    if (movie == null) {
                        _uiState.value =
                            MovieDetailUiState(
                                screenState =
                                    MovieDetailScreenState.Error(
                                        UiText.StringResource(SR.string.movie_detail_not_found),
                                    ),
                            )
                        return@fold
                    }
                    _uiState.update {
                        it.copy(
                            screenState = MovieDetailScreenState.Content,
                            movie = movie.toUi(),
                        )
                    }
                    checkTooltip(movieId, requestVersion)
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
                                    UiText.StringResource(SR.string.error_message_retry),
                                ),
                            movie = null,
                            showTooltip = false,
                            isMovieStateUpdatePending = false,
                        )
                    }
                },
            )
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
                _effect.emit(MovieDetailEffect.ShareMovie(movie))
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
            val requestVersion = activeRequestVersion
            mutationJob =
                viewModelScope.launch {
                    try {
                        val result =
                            updateMovieStateUseCase(
                                updatedMovie.toDomain(),
                            )
                        val exception = result.exceptionOrNull()
                        if (requestVersion != activeRequestVersion) return@launch
                        if (exception == null) {
                            observabilityTracker.trackAction(
                                MOVIE_DETAIL_TOGGLE_WATCHLIST,
                                mapOf(
                                    MOVIE_ID to movie.id.toString(),
                                    IS_ENABLED to isNowInWatchlist.toString(),
                                ),
                            )
                            _uiState.update { it.copy(movie = updatedMovie) }
                            enqueueUpload(movie.id)
                        } else {
                            observabilityTracker.recordError(
                                MOVIE_DETAIL_TOGGLE_WATCHLIST_FAILED,
                                exception,
                                mapOf(MOVIE_ID to movie.id.toString()),
                            )
                            _effect.emit(
                                MovieDetailEffect.ShowError(
                                    UiText.StringResource(SR.string.error_message_retry),
                                ),
                            )
                        }
                    } finally {
                        if (requestVersion == activeRequestVersion) {
                            _uiState.update { it.copy(isMovieStateUpdatePending = false) }
                        }
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
            val requestVersion = activeRequestVersion
            mutationJob =
                viewModelScope.launch {
                    try {
                        val result =
                            updateMovieStateUseCase(
                                updatedMovie.toDomain(),
                            )
                        val exception = result.exceptionOrNull()
                        if (requestVersion != activeRequestVersion) return@launch
                        if (exception == null) {
                            observabilityTracker.trackAction(
                                MOVIE_DETAIL_TOGGLE_WATCHED,
                                mapOf(
                                    MOVIE_ID to movie.id.toString(),
                                    IS_ENABLED to isNowWatched.toString(),
                                ),
                            )
                            _uiState.update { it.copy(movie = updatedMovie) }
                            enqueueUpload(movie.id)
                        } else {
                            observabilityTracker.recordError(
                                MOVIE_DETAIL_TOGGLE_WATCHED_FAILED,
                                exception,
                                mapOf(MOVIE_ID to movie.id.toString()),
                            )
                            _effect.emit(
                                MovieDetailEffect.ShowError(
                                    UiText.StringResource(SR.string.error_message_retry),
                                ),
                            )
                        }
                    } finally {
                        if (requestVersion == activeRequestVersion) {
                            _uiState.update { it.copy(isMovieStateUpdatePending = false) }
                        }
                    }
                }
        }

        private fun checkTooltip(
            movieId: Int,
            requestVersion: Int,
        ) {
            if (tooltipCheckedMovieId == movieId) return
            if (tooltipJob?.isActive == true && tooltipJobRequestVersion == requestVersion) return
            tooltipJob?.cancel()
            tooltipJobRequestVersion = requestVersion
            tooltipJob =
                viewModelScope.launch {
                    val hasSeen = observeHasSeenDetailTooltipUseCase().first()
                    val state = _uiState.value
                    if (
                        requestVersion != activeRequestVersion ||
                        state.screenState !is MovieDetailScreenState.Content ||
                        state.movie?.id != movieId
                    ) {
                        return@launch
                    }
                    tooltipCheckedMovieId = movieId
                    if (!hasSeen) {
                        _uiState.update { it.copy(showTooltip = true) }
                    }
                }
        }

        private fun enqueueUpload(movieId: Int) {
            try {
                syncScheduler.enqueueUpload(movieId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: IllegalStateException) {
                Unit
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
