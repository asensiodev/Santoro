package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedStatsUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.SearchWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.presentation.mapper.toUiList
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

@OptIn(FlowPreview::class)
@HiltViewModel
internal class WatchedMoviesViewModel
    @Inject
    constructor(
        private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase,
        private val getWatchedStatsUseCase: GetWatchedStatsUseCase,
        private val searchWatchedMoviesUseCase: SearchWatchedMoviesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(WatchedMoviesUiState())
        val uiState: StateFlow<WatchedMoviesUiState> = _uiState.asStateFlow()

        private val _effect = Channel<WatchedMoviesEffect>(Channel.BUFFERED)
        val effect = _effect.receiveAsFlow()

        private val searchQuery = MutableStateFlow("")

        fun process(intent: WatchedMoviesIntent) {
            when (intent) {
                is WatchedMoviesIntent.LoadMovies -> loadMovies()
                is WatchedMoviesIntent.LoadStats -> loadStats()
                is WatchedMoviesIntent.UpdateQuery -> updateQuery(intent.query)
            }
        }

        @OptIn(FlowPreview::class)
        private fun loadMovies() {
            fetchWatchedMovies()
            loadStats()

            searchQuery
                .drop(SKIP_INITIAL_EMISSION)
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
            showLoadingIfEmpty()
            viewModelScope.launch {
                getWatchedMoviesUseCase()
                    .collect { result ->
                        result.fold(
                            onSuccess = { moviesList ->
                                val movies = moviesList.toUiList()
                                val groupedMovies =
                                    movies.groupBy { movie ->
                                        movie.watchedDate.orEmpty()
                                    }
                                _uiState.update {
                                    it.copy(
                                        screenState =
                                            if (groupedMovies.isEmpty()) {
                                                WatchedScreenState.Empty
                                            } else {
                                                WatchedScreenState.Content
                                            },
                                        hasMovies = groupedMovies.isNotEmpty(),
                                        movies = groupedMovies,
                                    )
                                }
                            },
                            onFailure = { exception ->
                                _uiState.update {
                                    it.copy(
                                        screenState =
                                            WatchedScreenState.Error(
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
            _uiState.update { it.copy(query = query) }
            searchQuery.value = query
        }

        private fun searchWatchedMovies(query: String) {
            showLoadingIfEmpty()
            viewModelScope.launch {
                searchWatchedMoviesUseCase(query).collect { result ->
                    result.fold(
                        onSuccess = { moviesList ->
                            val moviesUi = moviesList.toUiList()
                            val groupedMovies =
                                moviesUi.groupBy { movie ->
                                    movie.watchedDate.orEmpty()
                                }
                            _uiState.update {
                                val screenState =
                                    if (groupedMovies.isEmpty()) {
                                        if (it.hasMovies == false) {
                                            WatchedScreenState.Empty
                                        } else {
                                            WatchedScreenState.NoResults
                                        }
                                    } else {
                                        WatchedScreenState.Content
                                    }
                                it.copy(
                                    screenState = screenState,
                                    movies = groupedMovies,
                                )
                            }
                        },
                        onFailure = { exception ->
                            _uiState.update {
                                it.copy(
                                    screenState =
                                        WatchedScreenState.Error(
                                            exception.message.orEmpty(),
                                        ),
                                )
                            }
                        },
                    )
                }
            }
        }

        private fun loadStats() {
            viewModelScope.launch {
                getWatchedStatsUseCase().collect { stats ->
                    _uiState.update { it.copy(stats = stats) }
                }
            }
        }

        private fun showLoadingIfEmpty() {
            _uiState.update { state ->
                if (state.movies.isEmpty()) {
                    state.copy(screenState = WatchedScreenState.Loading)
                } else {
                    state
                }
            }
        }
    }

private const val DELAY: Long = 500
private const val SKIP_INITIAL_EMISSION = 1
