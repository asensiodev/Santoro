package com.asensiodev.santoro.core.database

import com.asensiodev.santoro.core.database.data.model.MovieEntity

object MockUtils {
    fun createTestMovieEntity(
        id: Int,
        title: String = "Test Movie",
        isWatched: Boolean = false,
        isInWatchlist: Boolean = false,
        genres: String = "[]",
        productionCountries: String = "[]",
        overview: String = "Test Overview",
        posterPath: String? = null,
        releaseDate: String? = "2023-01-01",
        tagline: String? = null,
        runtime: Int? = null,
        watchedAt: Long? = null,
        updatedAt: Long = 0L,
    ) = MovieEntity(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        releaseDate = releaseDate,
        popularity = 7.5,
        voteAverage = 8.0,
        voteCount = 100,
        genres = genres,
        productionCountries = productionCountries,
        tagline = tagline,
        runtime = runtime,
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
        updatedAt = updatedAt,
    )
}
