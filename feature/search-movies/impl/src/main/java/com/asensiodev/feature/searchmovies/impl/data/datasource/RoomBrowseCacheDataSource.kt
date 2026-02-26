package com.asensiodev.feature.searchmovies.impl.data.datasource

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.data.model.BrowseCacheEntry
import com.asensiodev.santoro.core.data.mapper.toApiModel
import com.asensiodev.santoro.core.data.mapper.toDomain
import com.asensiodev.santoro.core.data.model.MovieApiModel
import com.asensiodev.santoro.core.database.data.dao.BrowseCacheDao
import com.asensiodev.santoro.core.database.data.model.BrowseCacheEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

internal class RoomBrowseCacheDataSource
    @Inject
    constructor(
        private val dao: BrowseCacheDao,
        private val gson: Gson,
    ) : BrowseCacheLocalDataSource {
        private val movieListType = object : TypeToken<List<MovieApiModel>>() {}.type

        override suspend fun getCachedPage(
            section: String,
            page: Int,
        ): BrowseCacheEntry? {
            val entity = dao.getPage(section, page) ?: return null
            val apiModels: List<MovieApiModel> = gson.fromJson(entity.moviesJson, movieListType)
            return BrowseCacheEntry(
                section = entity.section,
                page = entity.page,
                movies = apiModels.map { it.toDomain() },
                cachedAt = entity.cachedAt,
            )
        }

        override suspend fun savePage(
            section: String,
            page: Int,
            movies: List<Movie>,
            cachedAt: Long,
        ) {
            val apiModels = movies.map { it.toApiModel() }
            val json = gson.toJson(apiModels)
            dao.upsertPage(
                BrowseCacheEntity(
                    section = section,
                    page = page,
                    moviesJson = json,
                    cachedAt = cachedAt,
                ),
            )
        }

        override suspend fun clearSection(section: String) {
            dao.clearSection(section)
        }

        override suspend fun clearStaleEntries(cutoffMs: Long) {
            dao.clearEntriesOlderThan(cutoffMs)
        }
    }
