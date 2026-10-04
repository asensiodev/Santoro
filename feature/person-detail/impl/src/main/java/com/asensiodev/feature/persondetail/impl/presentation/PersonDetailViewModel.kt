package com.asensiodev.feature.persondetail.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@HiltViewModel
internal class PersonDetailViewModel
    @Inject
    constructor(
        private val repository: PersonRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PersonDetailUiState())
        val uiState = _uiState.asStateFlow()
        private var personId: Int? = null
        private var request: Job? = null

        fun process(intent: PersonDetailIntent) {
            when (intent) {
                is PersonDetailIntent.Initialize -> {
                    if (personId == null) {
                        personId = intent.personId
                        fetchProfile()
                    }
                }

                PersonDetailIntent.RetryProfile -> fetchProfile()
                PersonDetailIntent.RetryFilmography -> {
                    val id = personId ?: return
                    if (request?.isActive == true || _uiState.value.person == null) return
                    request = viewModelScope.launch { fetchFilmography(id) }
                }
            }
        }

        private fun fetchProfile() {
            val id = personId ?: return
            if (request?.isActive == true) return
            request =
                viewModelScope.launch {
                    _uiState.update { PersonDetailUiState() }
                    val result = repository.getPerson(id)
                    coroutineContext.ensureActive()
                    result.fold(
                        onSuccess = { person ->
                            _uiState.update {
                                it.copy(
                                    person = person,
                                    profileState = PersonLoadState.Content,
                                )
                            }
                            fetchFilmography(id)
                        },
                        onFailure = { error ->
                            if (error is CancellationException) throw error
                            _uiState.update { it.copy(profileState = PersonLoadState.Error) }
                        },
                    )
                }
        }

        private suspend fun fetchFilmography(id: Int) {
            _uiState.update { it.copy(filmographyState = PersonLoadState.Loading) }
            val result = repository.getMovieCredits(id)
            coroutineContext.ensureActive()
            result.fold(
                onSuccess = { credits ->
                    _uiState.update {
                        it.copy(
                            filmography = credits,
                            filmographyState = PersonLoadState.Content,
                        )
                    }
                },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    _uiState.update { it.copy(filmographyState = PersonLoadState.Error) }
                },
            )
        }
    }
