package com.asensiodev.feature.moviedetail.impl.presentation.mapper

import com.asensiodev.core.domain.Genre
import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.ProductionCountry
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi

internal fun Movie.toUi(): MovieUi =
    MovieUi(
        id = id,
        title = title,
        posterPath = posterPath?.let { BASE_POSTER_URL + it },
        overview = overview,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.map { it.name },
        productionCountries = productionCountries.map { it.name },
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
    )

internal fun MovieUi.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        posterPath = posterPath,
        overview = overview,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.map { Genre(it) },
        productionCountries = productionCountries.map { ProductionCountry(it) },
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
    )

private const val BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500"
