package com.asensiodev.feature.searchmovies.impl.data.model

import com.asensiodev.core.domain.model.Movie

internal data class BrowseCacheEntry(
    val section: String,
    val page: Int,
    val movies: List<Movie>,
    val cachedAt: Long,
)
