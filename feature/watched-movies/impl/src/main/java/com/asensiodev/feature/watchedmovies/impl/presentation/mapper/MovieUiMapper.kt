package com.asensiodev.feature.watchedmovies.impl.presentation.mapper

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun Movie.toUi(): MovieUi =
    MovieUi(
        id = id,
        title = title,
        posterPath = posterPath?.let { BASE_POSTER_URL + it },
        watchedDate =
            watchedAt?.let {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(it))
            },
        releaseYear = releaseYear()?.toString(),
        directors =
            directors?.takeIf { it.isNotEmpty() }?.joinToString(
                DIRECTOR_SEPARATOR,
            ) { it.name },
    )

internal fun List<Movie>.toUiList(): List<MovieUi> = this.map { it.toUi() }

private const val BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500"
private const val DIRECTOR_SEPARATOR = ", "
