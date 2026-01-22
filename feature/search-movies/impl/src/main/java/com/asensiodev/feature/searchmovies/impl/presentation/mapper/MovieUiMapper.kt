package com.asensiodev.feature.searchmovies.impl.presentation.mapper

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi

internal fun Movie.toUi(): MovieUi =
    MovieUi(
        id = id,
        title = title,
        posterPath = posterPath?.let { BASE_POSTER_URL + it },
        backdropPath = backdropPath?.let { BASE_BACKDROP_URL + it },
        voteAverage = voteAverage,
    )

internal fun List<Movie>.toUiList(): List<MovieUi> = this.map { it.toUi() }

private const val BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500"
private const val BASE_BACKDROP_URL = "https://image.tmdb.org/t/p/w780"
