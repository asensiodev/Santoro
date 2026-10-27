package com.asensiodev.feature.watchlist.impl.presentation

import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi

internal sealed interface WatchlistIntent {
    data object LoadMovies : WatchlistIntent
    data class UpdateQuery(
        val query: String,
    ) : WatchlistIntent
    data class RequestRemove(
        val movie: MovieUi,
    ) : WatchlistIntent
    data object ConfirmRemove : WatchlistIntent
    data object DismissRemoveDialog : WatchlistIntent
}
