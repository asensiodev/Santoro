package com.asensiodev.feature.persondetail.impl.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.asensiodev.feature.persondetail.api.navigation.PersonDetailRoute
import com.asensiodev.feature.persondetail.api.navigation.PersonFilmographyRoute
import com.asensiodev.feature.persondetail.impl.presentation.PersonDetailIntent
import com.asensiodev.feature.persondetail.impl.presentation.PersonDetailViewModel
import com.asensiodev.feature.persondetail.impl.presentation.PersonFilmographyScreen
import com.asensiodev.feature.persondetail.impl.presentation.PersonLoadState
import com.asensiodev.feature.persondetail.impl.presentation.PersonDetailRoute as ScreenRoute

fun NavController.navigateToPersonDetail(personId: Int) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        navigate(PersonDetailRoute(personId))
    }
}

fun NavGraphBuilder.personDetailRoute(
    navController: NavController,
    onBackClicked: () -> Unit,
    onMovieClicked: (Int) -> Unit,
) {
    composable<PersonDetailRoute> { entry ->
        val args = entry.toRoute<PersonDetailRoute>()
        ScreenRoute(args.personId, onBackClicked, onMovieClicked, onSeeAllClicked = { section ->
            if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
                navController.navigate(PersonFilmographyRoute(args.personId, section))
            }
        })
    }
}

fun NavGraphBuilder.personFilmographyRoute(
    navController: NavController,
    onBackClicked: () -> Unit,
    onMovieClicked: (Int) -> Unit,
) {
    composable<PersonFilmographyRoute> { entry ->
        val args = entry.toRoute<PersonFilmographyRoute>()
        val profileEntry =
            remember(entry) { navController.getBackStackEntry(PersonDetailRoute(args.personId)) }
        val viewModel = hiltViewModel<PersonDetailViewModel>(profileEntry)
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(
            viewModel,
        ) { viewModel.process(PersonDetailIntent.Initialize(args.personId)) }
        PersonFilmographyScreen(
            state,
            args.section,
            onBackClicked,
            onMovieClicked,
            onRetry = {
                viewModel.process(
                    if (state.profileState == PersonLoadState.Error) {
                        PersonDetailIntent.RetryProfile
                    } else {
                        PersonDetailIntent.RetryFilmography
                    },
                )
            },
        )
    }
}
