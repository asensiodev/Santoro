package com.asensiodev.feature.watchedmovies.impl.presentation.director

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.core.domain.repository.MovieRuntimeRepository
import com.asensiodev.core.domain.repository.PersonRepository
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.feature.watchedmovies.impl.domain.filter.directedFeatureFilms
import com.asensiodev.feature.watchedmovies.impl.domain.filter.splitShorts
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchlistMovieIdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class DirectorCollectionViewModel
    @Inject
    constructor(
        private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase,
        private val getWatchlistMovieIdsUseCase: GetWatchlistMovieIdsUseCase,
        private val personRepository: PersonRepository,
        private val movieRuntimeRepository: MovieRuntimeRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DirectorCollectionUiState())
        val uiState: StateFlow<DirectorCollectionUiState> = _uiState.asStateFlow()

        private var personId: Int? = null
        private var watchedByDirector: List<Movie> = emptyList()
        private var crewCredits: List<PersonMovieCredit>? = null
        private var fetchedRuntimes: Map<Int, Int> = emptyMap()
        private var watchlistIds: Set<Int> = emptySet()
        private var request: Job? = null

        fun process(intent: DirectorCollectionIntent) {
            when (intent) {
                is DirectorCollectionIntent.Initialize -> initialize(intent.personId, intent.name)
                DirectorCollectionIntent.RetryFilmography -> loadFilmography()
                is DirectorCollectionIntent.ChangeShowPending -> changeShowPending(intent.show)
            }
        }

        private fun initialize(
            id: Int,
            name: String,
        ) {
            if (personId != null) return
            personId = id
            _uiState.update { it.copy(directorName = name) }
            viewModelScope.launch { loadShowPending() }
            loadProfilePhoto(id)
            observeWatched(id)
            observeWatchlist()
            loadFilmography()
        }

        private fun loadProfilePhoto(id: Int) {
            viewModelScope.launch {
                val result = personRepository.getPerson(id)
                ensureActive()
                result
                    .onSuccess { person ->
                        _uiState.update { it.copy(profilePath = person.profilePath) }
                    }.onFailure { error -> if (error is CancellationException) throw error }
            }
        }

        private fun observeWatchlist() {
            viewModelScope.launch {
                getWatchlistMovieIdsUseCase().collect { ids ->
                    watchlistIds = ids
                    publishFilms()
                }
            }
        }

        private fun observeWatched(id: Int) {
            viewModelScope.launch {
                getWatchedMoviesUseCase().collect { result ->
                    result.onSuccess { movies ->
                        watchedByDirector =
                            movies.filter { movie -> movie.directors.orEmpty().any { it.id == id } }
                        publishFilms()
                    }
                }
            }
        }

        private fun loadFilmography() {
            val id = personId ?: return
            request?.cancel()
            request =
                viewModelScope.launch {
                    _uiState.update { it.copy(filmographyState = FilmographyState.LOADING) }
                    val result = personRepository.getMovieCredits(id)
                    ensureActive()
                    result.fold(
                        onSuccess = { filmography ->
                            fetchedRuntimes = runtimesFor(filmography.crew)
                            crewCredits = filmography.crew
                            publishFilms()
                        },
                        onFailure = { error ->
                            if (error is CancellationException) throw error
                            _uiState.update { it.copy(filmographyState = FilmographyState.ERROR) }
                        },
                    )
                }
        }

        private suspend fun runtimesFor(credits: List<PersonMovieCredit>): Map<Int, Int> {
            val stored = storedRuntimes()
            val missing =
                directedFeatureFilms(credits, watchedByDirector, LocalDate.now(clock))
                    .map { it.movieId }
                    .filterNot(stored::containsKey)
            return if (missing.isEmpty()) {
                emptyMap()
            } else {
                movieRuntimeRepository.getRuntimes(
                    missing,
                )
            }
        }

        private fun storedRuntimes(): Map<Int, Int> =
            watchedByDirector
                .mapNotNull { movie -> movie.runtime?.takeIf { it > 0 }?.let { movie.id to it } }
                .toMap()

        private fun publishFilms() {
            val credits = crewCredits
            val filmography =
                directedFeatureFilms(credits.orEmpty(), watchedByDirector, LocalDate.now(clock))
                    .map { film ->
                        film.copy(
                            isInWatchlist =
                                !film.isWatched && film.movieId in watchlistIds,
                        )
                    }.splitShorts(fetchedRuntimes + storedRuntimes())
            val films = filmography.features
            _uiState.update { state ->
                state.copy(
                    films = films,
                    shorts = filmography.shorts,
                    watchedCount = films.count { it.isWatched },
                    totalCount = credits?.let { films.size },
                    filmographyState =
                        if (credits !=
                            null
                        ) {
                            FilmographyState.CONTENT
                        } else {
                            state.filmographyState
                        },
                )
            }
        }

        private suspend fun loadShowPending() {
            val showPending =
                try {
                    userPreferencesRepository.directorShowPending.first()
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: IOException) {
                    return
                }
            _uiState.update { it.copy(showPending = showPending) }
        }

        private fun changeShowPending(show: Boolean) {
            if (_uiState.value.showPending == show) return
            _uiState.update { it.copy(showPending = show) }
            viewModelScope.launch {
                try {
                    userPreferencesRepository.setDirectorShowPending(show)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: IOException) {
                    Unit
                }
            }
        }
    }
