package com.asensiodev.feature.moviedetail.impl.presentation.model

import com.asensiodev.core.domain.model.CrewRole

data class CrewMemberUi(
    val name: String,
    val role: CrewRole,
)
