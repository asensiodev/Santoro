package com.asensiodev.santoro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun SantoroNavHost(
    navController: NavHostController,
    navigationScreens: NavigationScreens,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SearchMovies,
        modifier = modifier,
    ) {
        composable<NavRoutes.SearchMovies> {
            navigationScreens.searchMoviesScreen.Screen(
                onMovieClick = { movieId ->
                    // navController.navigate(NavRoutes.MovieDetail(movieId))
                },
            )
        }
//        composable<NavRoutes.WatchedMovies> {
//            WatchedMoviesScreen(
//                onMovieClick = { movieId ->
//                    navController.navigate(NavRoutes.MovieDetail(movieId))
//                },
//            )
//        }
//        composable<NavRoutes.Watchlist> {
//            WatchlistScreen(
//                onMovieClick = { movieId ->
//                    navController.navigate(NavRoutes.MovieDetail(movieId))
//                },
//            )
//        }
//        composable<NavRoutes.MovieDetail> { backStackEntry ->
//            // TODO(): review this part
//            val detail = backStackEntry.toRoute<NavRoutes.MovieDetail>()
//            MovieDetailScreen(
//                detail.movieId
//            )
//        }
    }
}
