package com.asensiodev.feature.watchlist.impl.presentation

internal sealed interface WatchlistEffect {
    data class NavigateToDetail(
        val movieId: Int,
    ) : WatchlistEffect
}
