package com.asensiodev.feature.moviedetail.impl.presentation.model

data class MovieUi(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String? = null,
    val releaseDate: String?,
    val popularity: Double,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<GenreUi>,
    val productionCountries: List<String>,
    val cast: List<CastMemberUi>,
    val keyCrew: List<CrewMemberUi> = emptyList(),
    val runtime: String?,
    val director: String?,
    val isWatched: Boolean,
    val isInWatchlist: Boolean,
    val watchedAt: Long? = null,
)
