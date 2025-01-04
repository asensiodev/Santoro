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
import com.asensiodev.santoro.navigation.NavRoutes
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

    fun navigateToTopLevelDestination(destination: NavRoutes) {
        clearMovieDetailFromBackStack()
        val navOptions =
            navOptions {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }

        navController.navigate(destination, navOptions)
    }

    private fun clearMovieDetailFromBackStack() {
        navController.popBackStack<NavRoutes.MovieDetail>(inclusive = true)
    }
}
