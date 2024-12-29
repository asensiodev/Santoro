package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SearchMoviesViewModel
    @Inject
    constructor(
        private val searchMoviesUseCase: SearchMoviesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SearchMoviesUiState())
        val uiState: StateFlow<SearchMoviesUiState> = _uiState.asStateFlow()

        fun updateQuery(query: String) {
            _uiState.update { it.copy(query = query) }
            if (query.isNotBlank()) {
                fetchMovies(query)
            } else {
                _uiState.update { it.copy(movies = emptyList()) }
            }
        }

        private fun fetchMovies(query: String) {
            viewModelScope.launch {
                searchMoviesUseCase(query)
                    .onEach { result ->
                        when (result) {
                            is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                            is Result.Success ->
                                _uiState.update {
                                    it.copy(isLoading = false, movies = result.data)
                                }

                            is Result.Error ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = result.exception.message,
                                    )
                                }
                        }
                    }.catch { e ->
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = e.message)
                        }
                    }.launchIn(this)
            }
        }
    }
