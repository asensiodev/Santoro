package com.asensiodev.feature.persondetail.impl.presentation

import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.feature.persondetail.api.navigation.FilmographySection
import com.asensiodev.santoro.core.stringresources.R as SR

internal data class PersonDetailUiState(
    val person: Person? = null,
    val profileState: PersonLoadState = PersonLoadState.Loading,
    val filmographyState: PersonLoadState = PersonLoadState.Loading,
    val filmography: PersonFilmography = PersonFilmography(emptyList(), emptyList()),
) {
    val listLoadState: PersonLoadState
        get() = if (profileState == PersonLoadState.Content) filmographyState else profileState

    val filmographySections: List<FilmographySection>
        get() =
            when (person?.knownForDepartment) {
                null, "Acting" -> listOf(FilmographySection.ACTING, FilmographySection.CREW)
                else -> listOf(FilmographySection.CREW, FilmographySection.ACTING)
            }

    fun movies(section: FilmographySection): List<PersonMovieCredit> =
        when (section) {
            FilmographySection.ACTING -> filmography.acting
            FilmographySection.CREW -> filmography.crew
        }

    fun moviesByYear(section: FilmographySection): Map<String?, List<PersonMovieCredit>> =
        movies(section).groupBy { it.releaseDate?.substringBefore('-') }
}

internal val FilmographySection.title: Int
    get() =
        when (this) {
            FilmographySection.ACTING -> SR.string.person_acting
            FilmographySection.CREW -> SR.string.person_crew
        }

internal sealed interface PersonLoadState {
    data object Loading : PersonLoadState

    data object Content : PersonLoadState

    data object Error : PersonLoadState
}

internal sealed interface PersonDetailIntent {
    data class Initialize(
        val personId: Int,
    ) : PersonDetailIntent

    data object RetryProfile : PersonDetailIntent

    data object RetryFilmography : PersonDetailIntent
}
