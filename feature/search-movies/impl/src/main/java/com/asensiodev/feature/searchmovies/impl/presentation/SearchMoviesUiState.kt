package com.asensiodev.feature.searchmovies.impl.presentation

import com.asensiodev.core.domain.Movie

data class SearchMoviesUiState(
    val query: String = "",
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
