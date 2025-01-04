package com.asensiodev.feature.watchedmovies.impl.presentation

import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi

internal data class WatchedMoviesUiState(
    val movies: List<MovieUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasResults: Boolean = false,
)
