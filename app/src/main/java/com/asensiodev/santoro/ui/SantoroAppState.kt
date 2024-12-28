package com.asensiodev.santoro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.asensiodev.santoro.navigation.TopLevelDestination

@Composable
fun rememberSantoroAppState(
    navController: NavHostController = rememberNavController(),
    // coroutineScope: CoroutineScope = rememberCoroutineScope(),
): SantoroAppState =
    remember(navController) {
        SantoroAppState(navController)
    }

@Stable
class SantoroAppState(
    val navController: NavHostController,
    // coroutineScope: CoroutineScope,
    // networkMonitor: NetworkMonitor,
) {
    val currentDestination: NavDestination?
        @Composable get() =
            navController
                .currentBackStackEntryAsState()
                .value
                ?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            return TopLevelDestination.entries.firstOrNull { topLevelDestination ->
                currentDestination?.hasRoute(topLevelDestination.route) == true
            }
        }

    /**
     * UI logic for navigating to a top level destination in the app.
     *
     * @param topLevelDestination: The destination the app needs to navigate to.
     */
    fun navigateToTopLevelDestination(destination: TopLevelDestination) {
        val navOptions =
            navOptions {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        navController.navigate(destination.route, navOptions)
    }

//    /**
//     * UI logic for navigating to a movie detail destination.
//     *
//     * @param movieId: The id of the movie the app needs to navigate to.
//     */
//    fun navigateToMovieDetail(movieId: Int) {
//        navController.navigate("movie_detail/$movieId")
//    }
}
