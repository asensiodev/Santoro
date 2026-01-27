package com.asensiodev.feature.moviedetail.impl.presentation.mapper

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.ProductionCountry
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi

internal fun Movie.toUi(): MovieUi =
    MovieUi(
        id = id,
        title = title,
        posterPath = posterPath?.let { BASE_POSTER_URL + it },
        backdropPath = backdropPath?.let { BASE_BACKDROP_URL + it },
        overview = overview,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.map { it.name },
        productionCountries = productionCountries.map { it.name },
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
    )

internal fun MovieUi.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        posterPath = posterPath?.removePrefix(BASE_POSTER_URL),
        backdropPath = backdropPath?.removePrefix(BASE_BACKDROP_URL),
        overview = overview,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.map { Genre(it) },
        productionCountries = productionCountries.map { ProductionCountry(it) },
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
    )

private const val BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500"
private const val BASE_BACKDROP_URL = "https://image.tmdb.org/t/p/w780"
