package com.asensiodev.feature.watchlist.impl.presentation

import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi

internal sealed interface WatchlistScreenState {
    data object Loading : WatchlistScreenState
    data object Content : WatchlistScreenState
    data class Error(
        val message: String,
    ) : WatchlistScreenState
    data object Empty : WatchlistScreenState
}

internal data class WatchlistMoviesUiState(
    val screenState: WatchlistScreenState = WatchlistScreenState.Loading,
    val query: String = "",
    val movies: List<MovieUi> = emptyList(),
    val movieToRemove: MovieUi? = null,
)
