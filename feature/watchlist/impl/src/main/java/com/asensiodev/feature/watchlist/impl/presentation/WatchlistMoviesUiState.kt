package com.asensiodev.feature.watchlist.impl.presentation

import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi

internal data class WatchlistMoviesUiState(
    val movies: List<MovieUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasResults: Boolean = false,
)
