package com.asensiodev.feature.moviedetail.api.navigation

import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailRoute(
    val movieId: Int,
)
