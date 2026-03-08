package com.asensiodev.feature.moviedetail.impl.presentation

internal sealed interface MovieDetailIntent {
    data class FetchDetails(
        val movieId: Int,
    ) : MovieDetailIntent
    data object ToggleWatched : MovieDetailIntent
    data object ToggleWatchlist : MovieDetailIntent
    data object ShareMovie : MovieDetailIntent
    data object Retry : MovieDetailIntent
    data object DismissTooltip : MovieDetailIntent
}
