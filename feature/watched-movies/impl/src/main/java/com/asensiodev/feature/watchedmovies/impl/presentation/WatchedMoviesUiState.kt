package com.asensiodev.feature.watchedmovies.impl.presentation

import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi

internal data class WatchedMoviesUiState(
    val movies: Map<String, List<MovieUi>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val query: String = "",
) {
    val hasResults: Boolean
        get() = movies.isNotEmpty()

    val totalWatchedMovies: Int
        get() = movies.values.sumOf { it.size }
}
