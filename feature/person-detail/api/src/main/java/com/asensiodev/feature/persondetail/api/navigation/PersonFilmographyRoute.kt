package com.asensiodev.feature.persondetail.api.navigation

import kotlinx.serialization.Serializable

@Serializable
data class PersonFilmographyRoute(
    val personId: Int,
    val section: FilmographySection,
)

@Serializable
enum class FilmographySection {
    ACTING,
    CREW,
}
