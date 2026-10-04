package com.asensiodev.feature.persondetail.impl.data

import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography
import com.asensiodev.core.domain.model.PersonMovieCredit
import java.time.LocalDate
import java.time.format.DateTimeParseException

internal fun PersonApiModel.toDomain(): Person {
    require(id > 0 && !name.isNullOrBlank())
    return Person(
        id = id,
        name = name,
        biography = biography.orEmpty().trim(),
        profilePath = profilePath.nonBlank(),
        birthday = birthday.validDate(),
        deathday = deathday.validDate(),
        birthplace = birthplace.nonBlank(),
        knownForDepartment = knownForDepartment.nonBlank(),
    )
}

internal fun PersonCreditsApiModel.toDomain(): PersonFilmography =
    PersonFilmography(
        acting = cast.orEmpty().toMovies { it.character },
        crew = crew.orEmpty().toMovies { it.job },
    )

private fun List<PersonCreditApiModel>.toMovies(
    role: (PersonCreditApiModel) -> String?,
): List<PersonMovieCredit> =
    filter { it.id > 0 && !it.title.isNullOrBlank() }
        .groupBy { it.id }
        .map { (id, credits) ->
            val movie = credits.first()
            PersonMovieCredit(
                movieId = id,
                title = movie.title.orEmpty(),
                posterPath = credits.firstNotNullOfOrNull { it.posterPath.nonBlank() },
                releaseDate = credits.firstNotNullOfOrNull { it.releaseDate.validDate() },
                roles = credits.mapNotNull { role(it).nonBlank() }.distinct(),
            )
        }.sortedWith(
            compareByDescending<PersonMovieCredit> { it.releaseDate }
                .thenBy { it.movieId },
        )

private fun String?.nonBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

private fun String?.validDate(): String? =
    try {
        this?.let { LocalDate.parse(it).toString() }
    } catch (_: DateTimeParseException) {
        null
    }
