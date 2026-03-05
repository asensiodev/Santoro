package com.asensiodev.feature.moviedetail.impl.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.asensiodev.feature.moviedetail.api.navigation.MovieDetailRoute
import com.asensiodev.feature.moviedetail.impl.presentation.MovieDetailRoute as ScreenRoute

private typealias EnterAnim =
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?

private typealias ExitAnim =
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?

fun NavController.navigateToMovieDetail(
    movieId: Int,
    navOptions: NavOptions? = null,
) {
    navigate(MovieDetailRoute(movieId), navOptions)
}

fun NavGraphBuilder.movieDetailRoute(
    onBackClicked: () -> Unit,
    enterTransition: EnterAnim? = null,
    exitTransition: ExitAnim? = null,
    popEnterTransition: EnterAnim? = null,
    popExitTransition: ExitAnim? = null,
) {
    composable<MovieDetailRoute>(
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
    ) { backStackEntry ->
        val args = backStackEntry.toRoute<MovieDetailRoute>()
        ScreenRoute(movieId = args.movieId, onBackClicked = onBackClicked)
    }
}
