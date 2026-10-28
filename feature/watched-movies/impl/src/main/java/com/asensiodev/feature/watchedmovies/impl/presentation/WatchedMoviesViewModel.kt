package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedStatsUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.SearchWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.presentation.mapper.toWatchedGroups
import com.asensiodev.library.observability.api.NoOpObservabilityTracker
import com.asensiodev.library.observability.api.ObservabilityTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
internal class WatchedMoviesViewModel
    @Inject
    constructor(
        private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase,
        private val getWatchedStatsUseCase: GetWatchedStatsUseCase,
        private val searchWatchedMoviesUseCase: SearchWatchedMoviesUseCase,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val observabilityTracker: ObservabilityTracker = NoOpObservabilityTracker,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(WatchedMoviesUiState())
        val uiState: StateFlow<WatchedMoviesUiState> = _uiState.asStateFlow()

        private val searchQuery = MutableStateFlow("")
        private var moviesJob: Job? = null
        private var statsJob: Job? = null
        private var latestMovies: List<Movie> = emptyList()
        private var preferencesLoaded = false
        private var collectionViewTracked = false

        fun process(intent: WatchedMoviesIntent) {
            when (intent) {
                is WatchedMoviesIntent.LoadMovies -> loadMovies()
                is WatchedMoviesIntent.LoadStats -> loadStats()
                is WatchedMoviesIntent.UpdateQuery -> updateQuery(intent.query)
                is WatchedMoviesIntent.ChangeViewMode -> changeViewMode(intent.mode)
                is WatchedMoviesIntent.ChangeGrouping -> changeGrouping(intent.grouping)
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
                    loadSavedPreferences()
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
                    latestMovies = moviesList
                    _uiState.update { state ->
                        val groupedMovies = moviesList.toWatchedGroups(state.grouping)
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
                            groups = groupedMovies,
                        )
                    }
                    trackCollectionViewed()
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

        private suspend fun loadSavedPreferences() {
            if (preferencesLoaded) return
            preferencesLoaded = true
            val viewMode = readPreference { userPreferencesRepository.watchedViewMode.first() }
            val grouping = readPreference { userPreferencesRepository.watchedGrouping.first() }
            _uiState.update { state ->
                state.copy(
                    viewMode = viewMode ?: state.viewMode,
                    grouping = grouping ?: state.grouping,
                )
            }
        }

        private fun changeViewMode(mode: WatchedViewMode) {
            if (_uiState.value.viewMode == mode) return
            _uiState.update { it.copy(viewMode = mode) }
            observabilityTracker.trackAction(
                VIEW_MODE_CHANGED,
                mapOf(
                    MODE_PARAMETER to mode.analyticsValue(),
                ),
            )
            persistPreference { userPreferencesRepository.setWatchedViewMode(mode) }
        }

        private fun changeGrouping(grouping: WatchedGrouping) {
            if (_uiState.value.grouping == grouping) return
            _uiState.update {
                it.copy(
                    grouping = grouping,
                    groups = latestMovies.toWatchedGroups(grouping),
                )
            }
            observabilityTracker.trackAction(
                GROUPING_CHANGED,
                mapOf(GROUPING_PARAMETER to grouping.analyticsValue()),
            )
            persistPreference { userPreferencesRepository.setWatchedGrouping(grouping) }
        }

        private fun trackCollectionViewed() {
            val state = _uiState.value
            if (collectionViewTracked || state.screenState !is WatchedScreenState.Content) return
            collectionViewTracked = true
            observabilityTracker.trackAction(
                COLLECTION_VIEWED,
                mapOf(
                    MODE_PARAMETER to state.viewMode.analyticsValue(),
                    GROUPING_PARAMETER to state.grouping.analyticsValue(),
                ),
            )
        }

        private suspend fun <T> readPreference(read: suspend () -> T): T? =
            try {
                read()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: IOException) {
                null
            }

        private fun persistPreference(write: suspend () -> Unit) {
            viewModelScope.launch {
                try {
                    write()
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: IOException) {
                    Unit
                }
            }
        }

        private fun showLoadingIfEmpty() {
            _uiState.update { state ->
                if (state.groups.isEmpty()) {
                    state.copy(screenState = WatchedScreenState.Loading)
                } else {
                    state
                }
            }
        }
    }

private fun Enum<*>.analyticsValue(): String = name.lowercase()

private const val DELAY: Long = 500
private const val COLLECTION_VIEWED = "watched_collection_viewed"
private const val VIEW_MODE_CHANGED = "watched_view_mode_changed"
private const val GROUPING_CHANGED = "watched_grouping_changed"
private const val MODE_PARAMETER = "mode"
private const val GROUPING_PARAMETER = "grouping"
private const val SKIP_INITIAL_EMISSION = 1
