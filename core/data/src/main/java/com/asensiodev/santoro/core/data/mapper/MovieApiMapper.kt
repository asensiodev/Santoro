package com.asensiodev.santoro.core.data.mapper

import com.asensiodev.core.domain.model.CastMember
import com.asensiodev.core.domain.model.CrewMember
import com.asensiodev.core.domain.model.CrewRole
import com.asensiodev.core.domain.model.Director
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.ProductionCountry
import com.asensiodev.santoro.core.data.model.CastMemberApiModel
import com.asensiodev.santoro.core.data.model.CreditsApiModel
import com.asensiodev.santoro.core.data.model.CrewMemberApiModel
import com.asensiodev.santoro.core.data.model.GenreApiModel
import com.asensiodev.santoro.core.data.model.MovieApiModel
import com.asensiodev.santoro.core.data.model.ProductionCountryApiModel

fun MovieApiModel.toDomain() =
    Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        popularity = popularity ?: 0.0,
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        genres = genres?.map { it.toDomain() } ?: emptyList(),
        genreIds = genreIds ?: genres?.map { it.id } ?: emptyList(),
        productionCountries = productionCountries?.map { it.toDomain() } ?: emptyList(),
        cast = credits?.cast?.map { it.toDomain() } ?: emptyList(),
        crew = credits?.crew?.map { it.toDomain() } ?: emptyList(),
        runtime = runtime,
        tagline = tagline,
        director = credits?.crew?.firstOrNull { it.job == DIRECTOR_JOB }?.name,
        directors = credits?.toDirectors(),
        isWatched = false,
        isInWatchlist = false,
        watchedAt = null,
    )

fun CreditsApiModel.toDirectors(): List<Director> =
    crew
        .orEmpty()
        .filter { it.job == DIRECTOR_JOB }
        .distinctBy { it.id }
        .map { Director(id = it.id, name = it.name) }

fun GenreApiModel.toDomain() =
    Genre(
        id = id,
        name = name,
    )

fun CastMemberApiModel.toDomain() =
    CastMember(
        id = id,
        creditId = creditId,
        name = name,
        character = character ?: "",
        profilePath = profilePath,
    )

fun CrewMemberApiModel.toDomain(): CrewMember {
    val role =
        when (job) {
            DIRECTOR_JOB -> CrewRole.DIRECTOR
            "Writer", "Screenplay", "Story" -> CrewRole.WRITER
            "Cinematographer", "Director of Photography" -> CrewRole.CINEMATOGRAPHER
            "Original Music Composer", "Music" -> CrewRole.COMPOSER
            else -> CrewRole.UNKNOWN
        }
    return CrewMember(
        id = id,
        name = name,
        role = role,
    )
}

fun ProductionCountryApiModel.toDomain() =
    ProductionCountry(
        name = name,
    )

fun Movie.toApiModel() =
    MovieApiModel(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genreIds = genreIds,
        genres = null,
        productionCountries = null,
        runtime = null,
        tagline = null,
        credits = null,
    )

private const val DIRECTOR_JOB = "Director"
