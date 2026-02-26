package com.asensiodev.santoro.core.database.data.model

import androidx.room.Entity

@Entity(tableName = "browse_cache", primaryKeys = ["section", "page"])
data class BrowseCacheEntity(
    val section: String,
    val page: Int,
    val moviesJson: String,
    val cachedAt: Long,
)
