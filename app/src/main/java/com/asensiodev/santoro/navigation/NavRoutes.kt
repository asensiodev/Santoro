package com.asensiodev.santoro.navigation

import kotlinx.serialization.Serializable

sealed class NavRoutes {
    @Serializable
    data object SearchMovies : NavRoutes()

    @Serializable
    data object WatchedMovies : NavRoutes()

    @Serializable
    data object Watchlist : NavRoutes()

    @Serializable
    data class MovieDetail(
        val movieId: Int,
    ) : NavRoutes()
}
