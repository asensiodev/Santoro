package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedStatsUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.SearchWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.presentation.mapper.toUiList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
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

        private val searchQuery = MutableStateFlow("")
        private var moviesJob: Job? = null
        private var statsJob: Job? = null

        fun process(intent: WatchedMoviesIntent) {
            when (intent) {
                is WatchedMoviesIntent.LoadMovies -> loadMovies()
                is WatchedMoviesIntent.LoadStats -> loadStats()
                is WatchedMoviesIntent.UpdateQuery -> updateQuery(intent.query)
            }
        }

        @OptIn(FlowPreview::class)
        private fun loadMovies() {
            if (moviesJob?.isActive != true ||
                _uiState.value.screenState is WatchedScreenState.Error
            ) {
                moviesJob?.cancel()
                observeMovies()
            }
            loadStats()
        }

        private fun observeMovies() {
            showLoadingIfEmpty()
            moviesJob =
                viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
                    searchQuery
                        .drop(SKIP_INITIAL_EMISSION)
                        .debounce(DELAY)
                        .onStart { emit(searchQuery.value) }
                        .distinctUntilChanged()
                        .flatMapLatest { query ->
                            val moviesFlow =
                                if (query.isBlank()) {
                                    getWatchedMoviesUseCase()
                                } else {
                                    searchWatchedMoviesUseCase(query)
                                }
                            moviesFlow.map { result -> query to result }
                        }.collect { (query, result) ->
                            updateMovies(result, query.isBlank())
                        }
                }
        }

        private fun updateQuery(query: String) {
            _uiState.update { it.copy(query = query) }
            searchQuery.value = query
        }

        private fun loadStats() {
            if (statsJob?.isActive == true) return
            statsJob =
                viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
                    getWatchedStatsUseCase().collect { stats ->
                        _uiState.update { it.copy(stats = stats) }
                    }
                }
        }

        private fun updateMovies(
            result: Result<List<Movie>>,
            isUnfiltered: Boolean,
        ) {
            result.fold(
                onSuccess = { moviesList ->
                    val groupedMovies =
                        moviesList
                            .toUiList()
                            .groupBy { movie -> movie.watchedDate.orEmpty() }
                    _uiState.update { state ->
                        val hasMovies =
                            if (isUnfiltered) {
                                groupedMovies.isNotEmpty()
                            } else {
                                state.hasMovies
                            }
                        val screenState =
                            when {
                                groupedMovies.isNotEmpty() -> WatchedScreenState.Content
                                hasMovies == false -> WatchedScreenState.Empty
                                else -> WatchedScreenState.NoResults
                            }
                        state.copy(
                            screenState = screenState,
                            hasMovies = hasMovies,
                            movies = groupedMovies,
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            screenState = WatchedScreenState.Error(exception.message.orEmpty()),
                        )
                    }
                },
            )
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
