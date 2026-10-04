package com.asensiodev.core.domain.model

data class PersonFilmography(
    val acting: List<PersonMovieCredit>,
    val crew: List<PersonMovieCredit>,
)

data class PersonMovieCredit(
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val releaseDate: String?,
    val roles: List<String>,
)
