package com.asensiodev.core.domain

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val releaseDate: String?,
    val popularity: Double,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>,
    val productionCountries: List<ProductionCountry>,
    val isWatched: Boolean,
    val isInWatchlist: Boolean,
)
