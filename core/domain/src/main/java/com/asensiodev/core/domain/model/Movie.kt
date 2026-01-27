package com.asensiodev.core.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val popularity: Double,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>,
    val productionCountries: List<ProductionCountry>,
    val cast: List<CastMember> = emptyList(),
    val runtime: Int? = null,
    val director: String? = null,
    val isWatched: Boolean,
    val isInWatchlist: Boolean,
    val watchedAt: Long? = null,
)
