package com.asensiodev.feature.searchmovies.impl.presentation.model

data class MovieUi(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
)
