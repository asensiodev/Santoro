package com.asensiodev.feature.moviedetail.impl.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.asensiodev.feature.moviedetail.api.navigation.MovieDetailRoute
import com.asensiodev.feature.moviedetail.impl.presentation.MovieDetailScreenRoute

fun NavController.navigateToMovieDetail(
    movieId: Int,
    navOptions: NavOptions? = null,
) {
    navigate(MovieDetailRoute(movieId), navOptions)
}

fun NavGraphBuilder.movieDetailRoute() {
    composable<MovieDetailRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<MovieDetailRoute>()
        MovieDetailScreenRoute(movieId = args.movieId)
    }
}
