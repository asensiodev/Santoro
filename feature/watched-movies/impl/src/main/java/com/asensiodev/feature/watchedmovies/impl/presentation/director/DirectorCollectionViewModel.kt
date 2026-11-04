package com.asensiodev.feature.watchedmovies.impl.presentation.director

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.core.domain.repository.PersonRepository
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.feature.watchedmovies.impl.domain.filter.directedFeatureFilms
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
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
        private val personRepository: PersonRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DirectorCollectionUiState())
        val uiState: StateFlow<DirectorCollectionUiState> = _uiState.asStateFlow()

        private var personId: Int? = null
        private var watchedByDirector: List<Movie> = emptyList()
        private var crewCredits: List<PersonMovieCredit>? = null
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
            observeWatched(id)
            loadFilmography()
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

        private fun publishFilms() {
            val credits = crewCredits
            val films =
                directedFeatureFilms(credits.orEmpty(), watchedByDirector, LocalDate.now(clock))
            _uiState.update { state ->
                state.copy(
                    films = films,
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
