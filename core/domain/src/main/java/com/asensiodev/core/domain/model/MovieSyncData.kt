package com.asensiodev.core.domain.model

data class MovieSyncData(
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val genres: List<Genre>,
    val runtime: Int?,
    val directors: List<Director>? = null,
    val isWatched: Boolean,
    val isInWatchlist: Boolean,
    val watchedAt: Long?,
    val updatedAt: Long,
)
