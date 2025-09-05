package com.asensiodev.feature.watchedmovies.impl.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.asensiodev.feature.watchedmovies.api.navigation.WatchedMoviesRoute
import com.asensiodev.feature.watchedmovies.impl.presentation.WatchedMoviesRoute

fun NavGraphBuilder.watchedMoviesRoute(onMovieClick: (Int) -> Unit) {
    composable<WatchedMoviesRoute> {
        WatchedMoviesRoute(onMovieClick = onMovieClick)
    }
}
