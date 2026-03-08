package com.asensiodev.feature.watchlist.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.feature.watchlist.impl.domain.usecase.GetWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.RemoveFromWatchlistUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.SearchWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.presentation.mapper.toUiList
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class WatchlistMoviesViewModel
    @Inject
    constructor(
        private val getWatchlistMoviesUseCase: GetWatchlistMoviesUseCase,
        private val searchWatchlistMoviesUseCase: SearchWatchlistMoviesUseCase,
        private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
        private val syncScheduler: WorkManagerSyncScheduler,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(WatchlistMoviesUiState())
        val uiState: StateFlow<WatchlistMoviesUiState> = _uiState.asStateFlow()

        private val _effect = Channel<WatchlistEffect>(Channel.BUFFERED)
        val effect = _effect.receiveAsFlow()

        private val searchQuery = MutableStateFlow("")

        fun process(intent: WatchlistIntent) {
            when (intent) {
                is WatchlistIntent.LoadMovies -> loadMovies()
                is WatchlistIntent.UpdateQuery -> updateQuery(intent.query)
                is WatchlistIntent.RequestRemove -> onRemoveMovieClicked(intent.movie)
                is WatchlistIntent.ConfirmRemove -> onRemoveConfirmed()
                is WatchlistIntent.DismissRemoveDialog -> onRemoveDismissed()
            }
        }

        @OptIn(FlowPreview::class)
        private fun loadMovies() {
            fetchWatchlistMovies()

            searchQuery
                .drop(SKIP_INITIAL_EMISSION)
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
            showLoadingIfEmpty()
            viewModelScope.launch {
                getWatchlistMoviesUseCase()
                    .collect { result ->
                        result.fold(
                            onSuccess = { movies ->
                                val moviesUi = movies.toUiList()
                                _uiState.update {
                                    it.copy(
                                        screenState =
                                            if (moviesUi.isEmpty()) {
                                                WatchlistScreenState.Empty
                                            } else {
                                                WatchlistScreenState.Content
                                            },
                                        movies = moviesUi,
                                    )
                                }
                            },
                            onFailure = { exception ->
                                _uiState.update {
                                    it.copy(
                                        screenState =
                                            WatchlistScreenState.Error(
                                                exception.message.orEmpty(),
                                            ),
                                    )
                                }
                            },
                        )
                    }
            }
        }

        private fun updateQuery(query: String) {
            searchQuery.value = query
            _uiState.update { it.copy(query = query) }
        }

        private fun searchWatchlistMovies(query: String) {
            showLoadingIfEmpty()
            viewModelScope.launch {
                searchWatchlistMoviesUseCase(query).collect { result ->
                    result.fold(
                        onSuccess = { movies ->
                            val moviesUi = movies.toUiList()
                            _uiState.update {
                                it.copy(
                                    screenState =
                                        if (moviesUi.isEmpty()) {
                                            WatchlistScreenState.Empty
                                        } else {
                                            WatchlistScreenState.Content
                                        },
                                    movies = moviesUi,
                                )
                            }
                        },
                        onFailure = { exception ->
                            _uiState.update {
                                it.copy(
                                    screenState =
                                        WatchlistScreenState.Error(
                                            exception.message.orEmpty(),
                                        ),
                                )
                            }
                        },
                    )
                }
            }
        }

        private fun showLoadingIfEmpty() {
            _uiState.update { state ->
                if (state.movies.isEmpty()) {
                    state.copy(screenState = WatchlistScreenState.Loading)
                } else {
                    state
                }
            }
        }

        private fun onRemoveMovieClicked(movie: MovieUi) {
            _uiState.update { it.copy(movieToRemove = movie) }
        }

        private fun onRemoveDismissed() {
            _uiState.update { it.copy(movieToRemove = null) }
        }

        private fun onRemoveConfirmed() {
            val movie = _uiState.value.movieToRemove ?: return
            _uiState.update { it.copy(movieToRemove = null) }
            viewModelScope.launch {
                removeFromWatchlistUseCase(movie.id)
                    .onSuccess { syncScheduler.enqueueUpload(movie.id) }
            }
        }
    }

private const val DELAY: Long = 500
private const val SKIP_INITIAL_EMISSION = 1
