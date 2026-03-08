package com.asensiodev.feature.searchmovies.impl.presentation.seeall

import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import com.asensiodev.feature.searchmovies.impl.presentation.model.SectionType

internal sealed interface SeeAllScreenState {
    data object Loading : SeeAllScreenState
    data object Content : SeeAllScreenState
    data class Error(
        val message: String,
    ) : SeeAllScreenState
    data object Empty : SeeAllScreenState
}

internal data class SeeAllMoviesUiState(
    val sectionType: SectionType = SectionType.TRENDING,
    val movies: List<MovieUi> = emptyList(),
    val screenState: SeeAllScreenState = SeeAllScreenState.Loading,
    val isLoadingMore: Boolean = false,
    val isEndReached: Boolean = false,
)
