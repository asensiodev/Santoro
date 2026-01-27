package com.asensiodev.santoro.core.data.mapper

import com.asensiodev.core.domain.model.CastMember
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.ProductionCountry
import com.asensiodev.santoro.core.data.model.CastMemberApiModel
import com.asensiodev.santoro.core.data.model.GenreApiModel
import com.asensiodev.santoro.core.data.model.MovieApiModel
import com.asensiodev.santoro.core.data.model.ProductionCountryApiModel

fun MovieApiModel.toDomain() =
    Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        popularity = popularity ?: 0.0,
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        genres = genres?.map { it.toDomain() } ?: emptyList(),
        productionCountries = productionCountries?.map { it.toDomain() } ?: emptyList(),
        cast = credits?.cast?.map { it.toDomain() } ?: emptyList(),
        runtime = runtime,
        director = credits?.crew?.firstOrNull { it.job == "Director" }?.name,
        isWatched = false,
        isInWatchlist = false,
        watchedAt = null,
    )

fun GenreApiModel.toDomain() =
    Genre(
        name = name,
    )

fun CastMemberApiModel.toDomain() =
    CastMember(
        id = id,
        name = name,
        character = character ?: "",
        profilePath = profilePath,
    )

fun ProductionCountryApiModel.toDomain() =
    ProductionCountry(
        name = name,
    )
