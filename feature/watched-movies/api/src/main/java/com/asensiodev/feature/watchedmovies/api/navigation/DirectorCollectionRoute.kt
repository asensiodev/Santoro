package com.asensiodev.feature.watchedmovies.api.navigation

import kotlinx.serialization.Serializable

@Serializable
data class DirectorCollectionRoute(
    val personId: Int,
    val name: String,
)
