package com.asensiodev.feature.watchlist.impl.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.asensiodev.feature.watchlist.api.navigation.WatchlistRoute
import com.asensiodev.feature.watchlist.impl.presentation.WatchlistMoviesRoute

fun NavGraphBuilder.watchlistRoute(onMovieClick: (Int) -> Unit) {
    composable<WatchlistRoute> {
        WatchlistMoviesRoute(onMovieClick = onMovieClick)
    }
}
