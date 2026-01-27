package com.asensiodev.santoro.core.database.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val releaseDate: String?,
    val popularity: Double,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: String,
    val productionCountries: String,
    val isWatched: Boolean = false,
    val isInWatchlist: Boolean = false,
    val watchedAt: Long? = null,
)
