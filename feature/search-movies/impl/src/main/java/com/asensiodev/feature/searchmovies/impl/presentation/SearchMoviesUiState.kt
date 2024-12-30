package com.asensiodev.feature.searchmovies.impl.presentation

import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi

internal data class SearchMoviesUiState(
    val query: String = "",
    val movies: List<MovieUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasResults: Boolean = false,
)
