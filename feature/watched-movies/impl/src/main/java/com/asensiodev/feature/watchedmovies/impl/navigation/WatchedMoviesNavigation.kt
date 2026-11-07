package com.asensiodev.feature.watchedmovies.impl.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.asensiodev.feature.watchedmovies.api.navigation.DirectorCollectionRoute
import com.asensiodev.feature.watchedmovies.api.navigation.WatchedMoviesRoute
import com.asensiodev.feature.watchedmovies.impl.presentation.WatchedMoviesRoute
import com.asensiodev.feature.watchedmovies.impl.presentation.director.DirectorCollectionRoute as DirectorCollectionScreenRoute

fun NavGraphBuilder.watchedMoviesRoute(
    onMovieClick: (Int) -> Unit,
    onDirectorClick: (Int, String) -> Unit,
) {
    composable<WatchedMoviesRoute> {
        WatchedMoviesRoute(onMovieClick = onMovieClick, onDirectorClick = onDirectorClick)
    }
}

fun NavController.navigateToDirectorCollection(
    personId: Int,
    name: String,
) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        navigate(DirectorCollectionRoute(personId, name))
    }
}

fun NavGraphBuilder.directorCollectionRoute(
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onDirectorProfileClick: (Int) -> Unit,
) {
    composable<DirectorCollectionRoute> { entry ->
        val args = entry.toRoute<DirectorCollectionRoute>()
        DirectorCollectionScreenRoute(
            personId = args.personId,
            name = args.name,
            onBackClick = onBackClick,
            onMovieClick = onMovieClick,
            onDirectorProfileClick = onDirectorProfileClick,
        )
    }
}
