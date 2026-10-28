package com.asensiodev.feature.watchedmovies.impl.presentation

import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode

internal sealed interface WatchedMoviesIntent {
    data object LoadMovies : WatchedMoviesIntent
    data object LoadStats : WatchedMoviesIntent
    data class UpdateQuery(
        val query: String,
    ) : WatchedMoviesIntent

    data class ChangeViewMode(
        val mode: WatchedViewMode,
    ) : WatchedMoviesIntent

    data class ChangeGrouping(
        val grouping: WatchedGrouping,
    ) : WatchedMoviesIntent
}
