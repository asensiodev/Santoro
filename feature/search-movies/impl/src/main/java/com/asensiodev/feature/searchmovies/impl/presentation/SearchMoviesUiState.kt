package com.asensiodev.feature.searchmovies.impl.presentation

import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi

internal data class SearchMoviesUiState(
    val query: String = "",
    val searchMovieResults: List<MovieUi> = emptyList(),
    val popularMovies: List<MovieUi> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isSearchLoading: Boolean = false,
    val isPopularMoviesLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearchResults: Boolean = false,
    val hasPopularMoviesResults: Boolean = false,
    val currentSearchPage: Int = 1,
    val currentPopularPage: Int = 1,
    val isSearchEndReached: Boolean = false,
    val isPopularEndReached: Boolean = false,
)
