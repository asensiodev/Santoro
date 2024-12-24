package com.asensiodev.core.domain

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val releaseDate: String?,
    val popularity: Double,
    val voteAverage: Double,
    val voteCount: Int,
)
