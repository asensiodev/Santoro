package com.asensiodev.feature.watchlist.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.watchlist.impl.domain.usecase.GetWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.RemoveFromWatchlistUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.SearchWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.presentation.mapper.toUiList
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

        private val searchQuery = MutableStateFlow("")
        private var moviesJob: Job? = null

        fun process(intent: WatchlistIntent) {
            when (intent) {
                is WatchlistIntent.LoadMovies -> loadMovies()
                is WatchlistIntent.UpdateQuery -> updateQuery(intent.query)
                is WatchlistIntent.RequestRemove -> onRemoveMovieClicked(intent.movie)
                is WatchlistIntent.ConfirmRemove -> onRemoveConfirmed()
                is WatchlistIntent.DismissRemoveDialog -> onRemoveDismissed()
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
        private fun loadMovies() {
            val isRetry = _uiState.value.screenState is WatchlistScreenState.Error
            if (moviesJob?.isActive == true && !isRetry) return
            moviesJob?.cancel()
            showLoadingIfEmpty()
            moviesJob =
                viewModelScope.launch {
                    searchQuery
                        .debounce { query -> if (query.isBlank()) NO_DELAY else SEARCH_DELAY }
                        .distinctUntilChanged()
                        .flatMapLatest { query ->
                            val movies =
                                if (query.isBlank()) {
                                    getWatchlistMoviesUseCase()
                                } else {
                                    searchWatchlistMoviesUseCase(query)
                                }
                            movies.map { result -> query to result }
                        }.collect { (query, result) ->
                            updateMovies(query, result)
                        }
                }
        }

        private fun updateMovies(
            query: String,
            result: Result<List<Movie>>,
        ) {
            result.fold(
                onSuccess = { movies ->
                    val moviesUi = movies.toUiList()
                    _uiState.update { state ->
                        val screenState = createScreenState(query, moviesUi, state.hasMovies)
                        val totalMoviesCount =
                            if (query.isBlank()) {
                                moviesUi.size
                            } else {
                                state.totalMoviesCount
                            }
                        state.copy(
                            screenState = screenState,
                            totalMoviesCount = totalMoviesCount,
                            hasMovies = totalMoviesCount > 0,
                            listHeader =
                                createListHeader(
                                    query = query,
                                    screenState = screenState,
                                    visibleMoviesCount = moviesUi.size,
                                ),
                            movies = moviesUi,
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            screenState = WatchlistScreenState.Error(exception.message.orEmpty()),
                            listHeader = null,
                        )
                    }
                },
            )
        }

        private fun createScreenState(
            query: String,
            movies: List<MovieUi>,
            hasMovies: Boolean?,
        ): WatchlistScreenState =
            when {
                movies.isNotEmpty() -> WatchlistScreenState.Content
                query.isBlank() -> WatchlistScreenState.Empty
                hasMovies == false -> WatchlistScreenState.Empty
                else -> WatchlistScreenState.NoResults
            }

        private fun updateQuery(query: String) {
            searchQuery.value = query
            _uiState.update { it.copy(query = query) }
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
            if (_uiState.value.isRemovingMovie) return
            _uiState.update { it.copy(movieToRemove = movie, hasRemoveError = false) }
        }

        private fun onRemoveDismissed() {
            if (_uiState.value.isRemovingMovie) return
            _uiState.update { it.copy(movieToRemove = null, hasRemoveError = false) }
        }

        private fun onRemoveConfirmed() {
            val state = _uiState.value
            val movie = state.movieToRemove ?: return
            if (state.isRemovingMovie) return
            _uiState.update { it.copy(isRemovingMovie = true, hasRemoveError = false) }
            viewModelScope.launch {
                try {
                    removeFromWatchlistUseCase(movie.id)
                        .onSuccess {
                            _uiState.update { it.copy(movieToRemove = null) }
                            try {
                                syncScheduler.enqueueUpload(movie.id)
                            } catch (exception: CancellationException) {
                                throw exception
                            } catch (_: IllegalStateException) {
                                Unit
                            }
                        }.onFailure {
                            _uiState.update { it.copy(hasRemoveError = true) }
                        }
                } finally {
                    _uiState.update { it.copy(isRemovingMovie = false) }
                }
            }
        }

        private fun createListHeader(
            query: String,
            screenState: WatchlistScreenState,
            visibleMoviesCount: Int,
        ): WatchlistListHeaderUi? {
            if (screenState !is WatchlistScreenState.Content || visibleMoviesCount == 0) {
                return null
            }

            return if (query.isBlank()) {
                WatchlistListHeaderUi.MoviesToWatch(count = visibleMoviesCount)
            } else {
                WatchlistListHeaderUi.SearchResults(count = visibleMoviesCount)
            }
        }
    }

private const val SEARCH_DELAY = 500L
private const val NO_DELAY = 0L
