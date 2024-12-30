package com.asensiodev.feature.searchmovies.impl.presentation.mapper

import com.asensiodev.core.domain.Movie
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi

internal fun Movie.toUi(): MovieUi =
    MovieUi(
        id = id,
        title = title,
        posterPath = posterPath?.let { BASE_POSTER_URL + it },
    )

internal fun List<Movie>.toUiList(): List<MovieUi> = this.map { it.toUi() }

private const val BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500"
