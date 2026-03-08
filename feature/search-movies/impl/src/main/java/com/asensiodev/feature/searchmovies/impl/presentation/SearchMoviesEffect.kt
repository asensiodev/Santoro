package com.asensiodev.feature.searchmovies.impl.presentation

import com.asensiodev.feature.searchmovies.impl.presentation.model.SectionType

internal sealed interface SearchMoviesEffect {
    data class NavigateToDetail(
        val movieId: Int,
    ) : SearchMoviesEffect
    data object ShowRefreshSuccess : SearchMoviesEffect
    data class NavigateToSeeAll(
        val sectionType: SectionType,
    ) : SearchMoviesEffect
}
