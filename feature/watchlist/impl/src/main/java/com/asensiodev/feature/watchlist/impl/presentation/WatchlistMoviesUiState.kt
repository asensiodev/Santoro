package com.asensiodev.feature.watchlist.impl.presentation

import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi

internal sealed interface WatchlistScreenState {
    data object Loading : WatchlistScreenState
    data object Content : WatchlistScreenState
    data object NoResults : WatchlistScreenState
    data class Error(
        val message: String,
    ) : WatchlistScreenState
    data object Empty : WatchlistScreenState
}

internal sealed interface WatchlistListHeaderUi {
    data class MoviesToWatch(
        val count: Int,
    ) : WatchlistListHeaderUi

    data class SearchResults(
        val count: Int,
    ) : WatchlistListHeaderUi
}

internal data class WatchlistMoviesUiState(
    val screenState: WatchlistScreenState = WatchlistScreenState.Loading,
    val query: String = "",
    val movies: List<MovieUi> = emptyList(),
    val totalMoviesCount: Int = 0,
    val hasMovies: Boolean? = null,
    val listHeader: WatchlistListHeaderUi? = null,
    val movieToRemove: MovieUi? = null,
)
