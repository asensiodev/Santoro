package com.asensiodev.santoro.navigation

import com.asensiodev.feature.searchmovies.impl.presentation.SearchMoviesScreen
import javax.inject.Inject

data class NavigationScreens
    @Inject
    constructor(
        val searchMoviesScreen: SearchMoviesScreen,
    )
