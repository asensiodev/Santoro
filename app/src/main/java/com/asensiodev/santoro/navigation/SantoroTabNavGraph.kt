package com.asensiodev.santoro.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.asensiodev.feature.moviedetail.impl.presentation.navigation.navigateToMovieDetail
import com.asensiodev.feature.searchmovies.impl.navigation.searchMoviesRoute
import com.asensiodev.feature.watchedmovies.impl.navigation.watchedMoviesRoute
import com.asensiodev.feature.watchlist.impl.navigation.watchlistRoute

@Composable
fun SantoroTabNavGraph(
    navController: NavHostController,
    mainNavController: NavHostController,
    paddingValues: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = com.asensiodev.feature.searchmovies.api.navigation.SearchMoviesRoute,
        modifier = Modifier.padding(paddingValues),
    ) {
        searchMoviesRoute(
            onMovieClick = { movieId ->
                mainNavController.navigateToMovieDetail(movieId)
            },
        )

        watchedMoviesRoute(
            onMovieClick = { movieId ->
                mainNavController.navigateToMovieDetail(movieId)
            },
        )

        watchlistRoute(
            onMovieClick = { movieId ->
                mainNavController.navigateToMovieDetail(movieId)
            },
        )
    }
}
