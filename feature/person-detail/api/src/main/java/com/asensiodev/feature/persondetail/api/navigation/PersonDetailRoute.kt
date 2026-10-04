package com.asensiodev.feature.persondetail.api.navigation

import kotlinx.serialization.Serializable

@Serializable
data class PersonDetailRoute(
    val personId: Int,
)
