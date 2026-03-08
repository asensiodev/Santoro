package com.asensiodev.feature.searchmovies.impl.presentation.seeall

internal sealed interface SeeAllMoviesEffect {
    data class NavigateToDetail(
        val movieId: Int,
    ) : SeeAllMoviesEffect
}
