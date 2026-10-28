package com.asensiodev.santoro.core.sync.data.model

data class MovieSyncEntity(
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val genres: String,
    val runtime: Int?,
    val directors: String? = null,
    val isWatched: Boolean,
    val isInWatchlist: Boolean,
    val watchedAt: Long?,
    val updatedAt: Long,
)
