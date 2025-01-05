package com.asensiodev.feature.searchmovies.impl.presentation

import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi

internal data class SearchMoviesUiState(
    val query: String = "",
    val searchMovieResults: List<MovieUi> = emptyList(),
    val popularMovies: List<MovieUi> = emptyList(),
    val isSearchLoading: Boolean = false,
    val isPopularMoviesLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearchResults: Boolean = false,
    val hasPopularMoviesResults: Boolean = false,
)
