package com.asensiodev.feature.watchedmovies.impl.presentation

import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.feature.watchedmovies.impl.domain.model.WatchedStats
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupUi

internal sealed interface WatchedScreenState {
    data object Loading : WatchedScreenState
    data object Content : WatchedScreenState
    data object NoResults : WatchedScreenState
    data class Error(
        val message: String,
    ) : WatchedScreenState
    data object Empty : WatchedScreenState
}

internal data class WatchedMoviesUiState(
    val screenState: WatchedScreenState = WatchedScreenState.Loading,
    val groups: List<WatchedGroupUi> = emptyList(),
    val viewMode: WatchedViewMode = WatchedViewMode.SHELF,
    val grouping: WatchedGrouping = WatchedGrouping.DATE_WATCHED,
    val query: String = "",
    val stats: WatchedStats? = null,
    val hasMovies: Boolean? = null,
)
