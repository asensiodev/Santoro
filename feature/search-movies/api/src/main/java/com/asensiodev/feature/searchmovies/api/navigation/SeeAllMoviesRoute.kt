package com.asensiodev.feature.searchmovies.api.navigation

import kotlinx.serialization.Serializable

@Serializable
data class SeeAllMoviesRoute(
    val sectionType: String,
)
