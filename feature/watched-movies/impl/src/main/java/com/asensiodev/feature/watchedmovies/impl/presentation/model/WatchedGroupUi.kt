package com.asensiodev.feature.watchedmovies.impl.presentation.model

internal data class WatchedGroupUi(
    val key: String,
    val label: WatchedGroupLabel,
    val movies: List<MovieUi>,
    val directorId: Int? = null,
)

internal sealed interface WatchedGroupLabel {
    data class Text(
        val value: String,
    ) : WatchedGroupLabel

    data class Decade(
        val startYear: Int,
    ) : WatchedGroupLabel

    data object UnknownDate : WatchedGroupLabel

    data object UnknownDirector : WatchedGroupLabel

    data object NoGenre : WatchedGroupLabel

    data object Undated : WatchedGroupLabel
}
