package com.asensiodev.santoro.core.data.mapper

import com.asensiodev.core.domain.Genre
import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.ProductionCountry
import com.asensiodev.santoro.core.data.model.GenreApiModel
import com.asensiodev.santoro.core.data.model.MovieApiModel
import com.asensiodev.santoro.core.data.model.ProductionCountryApiModel

fun MovieApiModel.toDomain() =
    Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        releaseDate = releaseDate,
        popularity = popularity ?: 0.0,
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        genres = genres?.map { it.toDomain() } ?: emptyList(),
        productionCountries = productionCountries?.map { it.toDomain() } ?: emptyList(),
        isWatched = false,
        isInWatchlist = false,
    )

fun GenreApiModel.toDomain() =
    Genre(
        name = name,
    )

fun ProductionCountryApiModel.toDomain() =
    ProductionCountry(
        name = name,
    )
