package com.asensiodev.santoro.navigation

import com.asensiodev.feature.moviedetail.impl.presentation.MovieDetailScreen
import com.asensiodev.feature.searchmovies.impl.presentation.SearchMoviesScreen
import com.asensiodev.feature.watchedmovies.impl.presentation.WatchedMoviesScreen
import com.asensiodev.feature.watchlist.impl.presentation.WatchlistMoviesScreen
import javax.inject.Inject

data class NavigationScreens
    @Inject
    constructor(
        val searchMoviesScreen: SearchMoviesScreen,
        val watchedMoviesScreen: WatchedMoviesScreen,
        val watchlistScreen: WatchlistMoviesScreen,
        val movieDetailScreen: MovieDetailScreen,
    )
