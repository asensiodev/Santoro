package com.asensiodev.feature.watchedmovies.impl.presentation

internal sealed interface WatchedMoviesEffect {
    data class NavigateToDetail(
        val movieId: Int,
    ) : WatchedMoviesEffect
}
