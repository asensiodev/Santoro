package com.asensiodev.feature.watchlist.impl.presentation.mapper

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi

internal fun Movie.toUi(): MovieUi =
    MovieUi(
        id = id,
        title = title,
        posterPath = posterPath?.let { BASE_POSTER_URL + it },
        releaseYear = releaseDate?.take(YEAR_LENGTH),
        genres = genres.toDisplayGenres(),
        rating = voteAverage,
    )

internal fun List<Movie>.toUiList(): List<MovieUi> = this.map { it.toUi() }

private fun List<Genre>.toDisplayGenres(): String? =
    mapNotNull { genre ->
        runCatching { genre.name }
            .getOrNull()
            .toValidGenreName()
    }.joinToString(", ")
        .ifBlank { null }

private fun String?.toValidGenreName(): String? =
    this?.takeUnless { genreName ->
        genreName.isBlank() || genreName.equals(NULL_LITERAL, ignoreCase = true)
    }

private const val BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500"
private const val YEAR_LENGTH = 4
private const val NULL_LITERAL = "null"
