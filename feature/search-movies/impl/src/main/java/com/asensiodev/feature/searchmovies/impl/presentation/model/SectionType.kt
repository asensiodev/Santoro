package com.asensiodev.feature.searchmovies.impl.presentation.model

import com.asensiodev.santoro.core.stringresources.R

internal enum class SectionType(
    val key: String,
    val titleRes: Int,
) {
    TRENDING("trending", R.string.search_movies_trending_title),
    POPULAR("popular", R.string.search_movies_popular_movies_title),
    TOP_RATED("top_rated", R.string.search_movies_top_rated_title),
    UPCOMING("upcoming", R.string.search_movies_upcoming_title),
    ;

    companion object {
        fun fromKey(key: String): SectionType = entries.first { it.key == key }
    }
}
