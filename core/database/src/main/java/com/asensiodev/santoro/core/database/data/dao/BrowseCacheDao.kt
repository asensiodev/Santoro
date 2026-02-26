package com.asensiodev.santoro.core.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.asensiodev.santoro.core.database.data.model.BrowseCacheEntity

@Dao
interface BrowseCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPage(entry: BrowseCacheEntity)

    @Query("SELECT * FROM browse_cache WHERE section = :section AND page = :page LIMIT 1")
    suspend fun getPage(
        section: String,
        page: Int,
    ): BrowseCacheEntity?

    @Query("DELETE FROM browse_cache WHERE section = :section")
    suspend fun clearSection(section: String)

    @Query("DELETE FROM browse_cache WHERE cachedAt < :cutoff")
    suspend fun clearEntriesOlderThan(cutoff: Long)

    @Query("DELETE FROM browse_cache")
    suspend fun clearAll()
}
