package com.asensiodev.feature.searchmovies.impl.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.asensiodev.feature.searchmovies.api.navigation.SearchMoviesRoute
import com.asensiodev.feature.searchmovies.impl.presentation.SearchMoviesRoute

fun NavGraphBuilder.searchMoviesRoute(onMovieClick: (Int) -> Unit) {
    composable<SearchMoviesRoute> {
        SearchMoviesRoute(onMovieClick = onMovieClick)
    }
}
