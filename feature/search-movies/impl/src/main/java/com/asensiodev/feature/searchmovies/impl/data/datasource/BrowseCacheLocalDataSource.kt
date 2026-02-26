package com.asensiodev.feature.searchmovies.impl.data.datasource

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.data.model.BrowseCacheEntry

internal interface BrowseCacheLocalDataSource {
    suspend fun getCachedPage(
        section: String,
        page: Int,
    ): BrowseCacheEntry?
    suspend fun savePage(
        section: String,
        page: Int,
        movies: List<Movie>,
        cachedAt: Long,
    )
    suspend fun clearSection(section: String)
    suspend fun clearStaleEntries(cutoffMs: Long)
}
