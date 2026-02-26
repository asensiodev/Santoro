package com.asensiodev.feature.watchedmovies.impl.presentation

internal sealed interface WatchedMoviesIntent {
    data object LoadMovies : WatchedMoviesIntent
    data class UpdateQuery(
        val query: String,
    ) : WatchedMoviesIntent
}
