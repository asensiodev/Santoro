package com.asensiodev.feature.watchedmovies.impl.presentation

import com.asensiodev.feature.watchedmovies.impl.domain.model.WatchedStats
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi

internal sealed interface WatchedScreenState {
    data object Loading : WatchedScreenState
    data object Content : WatchedScreenState
    data class Error(
        val message: String,
    ) : WatchedScreenState
    data object Empty : WatchedScreenState
}

internal data class WatchedMoviesUiState(
    val screenState: WatchedScreenState = WatchedScreenState.Loading,
    val movies: Map<String, List<MovieUi>> = emptyMap(),
    val query: String = "",
    val stats: WatchedStats? = null,
)
