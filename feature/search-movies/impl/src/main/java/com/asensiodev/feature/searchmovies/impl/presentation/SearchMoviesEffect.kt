package com.asensiodev.feature.searchmovies.impl.presentation

internal sealed interface SearchMoviesEffect {
    data class NavigateToDetail(
        val movieId: Int,
    ) : SearchMoviesEffect
}
