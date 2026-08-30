package com.asensiodev.feature.searchmovies.impl.presentation

import com.asensiodev.feature.searchmovies.impl.presentation.model.SectionType

internal sealed interface SearchMoviesEffect {
    data object ShowRefreshSuccess : SearchMoviesEffect
}

internal sealed interface SearchMoviesNavigationEffect {
    data class NavigateToDetail(
        val movieId: Int,
    ) : SearchMoviesNavigationEffect

    data class NavigateToSeeAll(
        val sectionType: SectionType,
    ) : SearchMoviesNavigationEffect
}
