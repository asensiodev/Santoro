package com.asensiodev.santoro.core.sync.data.model

data class MovieSyncEntity(
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val isWatched: Boolean,
    val isInWatchlist: Boolean,
    val watchedAt: Long?,
    val updatedAt: Long,
)
