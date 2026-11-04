package com.asensiodev.feature.watchedmovies.impl.domain.model

internal data class DirectorFilm(
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val releaseDate: String?,
    val isWatched: Boolean,
)
