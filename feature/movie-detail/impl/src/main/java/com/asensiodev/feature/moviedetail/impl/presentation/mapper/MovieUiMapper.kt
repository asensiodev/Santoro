package com.asensiodev.feature.moviedetail.impl.presentation.mapper

import com.asensiodev.core.domain.model.CastMember
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.ProductionCountry
import com.asensiodev.feature.moviedetail.impl.presentation.model.CastMemberUi
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi

internal fun Movie.toUi(): MovieUi =
    MovieUi(
        id = id,
        title = title,
        posterPath = posterPath?.let { BASE_POSTER_URL + it },
        backdropPath = backdropPath?.let { BASE_BACKDROP_URL + it },
        overview = overview,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.map { it.name },
        productionCountries = productionCountries.map { it.name },
        cast = cast.map { it.toUi() },
        runtime = runtime?.let { formatRuntime(it) },
        director = director,
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
    )

internal fun MovieUi.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        posterPath = posterPath?.removePrefix(BASE_POSTER_URL),
        backdropPath = backdropPath?.removePrefix(BASE_BACKDROP_URL),
        overview = overview,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.map { Genre(it) },
        productionCountries = productionCountries.map { ProductionCountry(it) },
        cast = cast.map { it.toDomain() },
        runtime = null,
        director = director,
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
    )

private fun CastMember.toUi() =
    CastMemberUi(
        id = id,
        name = name,
        character = character,
        profileUrl = profilePath?.let { BASE_PROFILE_URL + it },
    )

private fun CastMemberUi.toDomain() =
    CastMember(
        id = id,
        name = name,
        character = character,
        profilePath = profileUrl?.removePrefix(BASE_PROFILE_URL),
    )

private const val BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500"
private const val BASE_BACKDROP_URL = "https://image.tmdb.org/t/p/w780"
private const val BASE_PROFILE_URL = "https://image.tmdb.org/t/p/w185"

private fun formatRuntime(minutes: Int): String {
    val hours = minutes / HOUR_MINUTES
    val remainingMinutes = minutes % MINUTE_SECONDS
    return if (hours > 0) {
        "${hours}h ${remainingMinutes}m"
    } else {
        "${remainingMinutes}m"
    }
}

private const val HOUR_MINUTES = 60
private const val MINUTE_SECONDS = 60
