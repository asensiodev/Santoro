package com.asensiodev.feature.moviedetail.impl.presentation.model

data class MovieUi(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val releaseDate: String?,
    val popularity: Double,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<String>,
    val productionCountries: List<String>,
    val isWatched: Boolean,
    val isInWatchlist: Boolean,
)
