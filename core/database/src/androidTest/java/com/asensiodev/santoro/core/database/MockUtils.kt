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
    ) = MovieEntity(
        id = id,
        title = title,
        overview = "Test Overview",
        posterPath = null,
        releaseDate = "2023-01-01",
        popularity = 7.5,
        voteAverage = 8.0,
        voteCount = 100,
        genres = genres,
        productionCountries = productionCountries,
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
    )
}
