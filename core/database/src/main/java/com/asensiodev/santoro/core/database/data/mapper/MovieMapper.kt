package com.asensiodev.santoro.core.database.data.mapper

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.ProductionCountry
import com.asensiodev.santoro.core.database.data.model.MovieEntity
import com.google.gson.Gson

fun MovieEntity.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = null,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.toGenres(),
        productionCountries = productionCountries.toProductionCountries(),
        tagline = tagline,
        runtime = runtime,
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
        updatedAt = updatedAt,
    )

fun String.toGenres(): List<Genre> {
    if (isBlank()) return emptyList()
    val gson = Gson()
    return gson.fromJson(this, Array<Genre>::class.java)?.toList().orEmpty()
}

fun String.toProductionCountries(): List<ProductionCountry> {
    if (isBlank()) return emptyList()
    val gson = Gson()
    return gson.fromJson(this, Array<ProductionCountry>::class.java)?.toList().orEmpty()
}

fun Movie.toEntity(): MovieEntity {
    val gson = Gson()
    return MovieEntity(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = gson.toJson(genres),
        productionCountries = gson.toJson(productionCountries),
        tagline = tagline,
        runtime = runtime,
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
        updatedAt = System.currentTimeMillis(),
    )
}
