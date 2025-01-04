package com.asensiodev.santoro.core.database.data.mapper

import com.asensiodev.core.domain.Genre
import com.asensiodev.core.domain.MovieDetail
import com.asensiodev.core.domain.ProductionCountry
import com.asensiodev.santoro.core.database.data.model.MovieEntity
import com.google.gson.Gson

fun MovieEntity.toDomain(): MovieDetail =
    MovieDetail(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.toGenres(),
        productionCountries = productionCountries.toProductionCountries(),
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
    )

fun String.toGenres(): List<Genre> {
    val gson = Gson()
    return gson.fromJson(this, Array<Genre>::class.java).toList()
}

fun String.toProductionCountries(): List<ProductionCountry> {
    val gson = Gson()
    return gson.fromJson(this, Array<ProductionCountry>::class.java).toList()
}

fun MovieDetail.toEntity(): MovieEntity {
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
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
    )
}
