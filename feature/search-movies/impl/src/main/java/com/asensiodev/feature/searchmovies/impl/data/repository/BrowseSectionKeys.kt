package com.asensiodev.feature.searchmovies.impl.data.repository

internal object BrowseSectionKeys {
    const val NOW_PLAYING = "now_playing"
    const val POPULAR = "popular"
    const val TOP_RATED = "top_rated"
    const val UPCOMING = "upcoming"
    const val TRENDING = "trending"

    fun searchKey(query: String): String = "search:${query.lowercase().trim()}"
}
