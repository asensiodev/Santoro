package com.asensiodev.feature.searchmovies.impl.domain.repository

import kotlinx.coroutines.flow.Flow

interface RecentSearchesRepository {
    fun getRecentSearches(): Flow<List<String>>
    suspend fun saveSearch(query: String)
    suspend fun clearAll()
}
