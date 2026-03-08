package com.asensiodev.feature.searchmovies.impl.presentation.seeall

internal sealed interface SeeAllMoviesIntent {
    data object LoadInitial : SeeAllMoviesIntent
    data object LoadMore : SeeAllMoviesIntent
    data class MovieClicked(
        val movieId: Int,
    ) : SeeAllMoviesIntent
    data object Retry : SeeAllMoviesIntent
}
