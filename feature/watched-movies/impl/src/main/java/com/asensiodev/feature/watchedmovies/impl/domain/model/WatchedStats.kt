package com.asensiodev.feature.watchedmovies.impl.domain.model

data class WatchedStats(
    val totalWatched: Int,
    val totalRuntimeHours: Int,
    val favouriteGenre: String?,
    val longestStreakWeeks: Int,
)
