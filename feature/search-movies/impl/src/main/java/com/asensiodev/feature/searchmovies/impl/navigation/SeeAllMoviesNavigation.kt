package com.asensiodev.feature.searchmovies.impl.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.asensiodev.feature.searchmovies.api.navigation.SeeAllMoviesRoute
import com.asensiodev.feature.searchmovies.impl.presentation.seeall.SeeAllMoviesRoute as SeeAllScreen

private typealias EnterAnim =
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?

private typealias ExitAnim =
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?

fun NavController.navigateToSeeAllMovies(
    sectionType: String,
    navOptions: NavOptions? = null,
) {
    navigate(SeeAllMoviesRoute(sectionType), navOptions)
}

fun NavGraphBuilder.seeAllMoviesRoute(
    onMovieClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    enterTransition: EnterAnim? = null,
    exitTransition: ExitAnim? = null,
    popEnterTransition: EnterAnim? = null,
    popExitTransition: ExitAnim? = null,
) {
    composable<SeeAllMoviesRoute>(
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
    ) {
        SeeAllScreen(
            onMovieClick = onMovieClick,
            onBackClick = onBackClick,
        )
    }
}
