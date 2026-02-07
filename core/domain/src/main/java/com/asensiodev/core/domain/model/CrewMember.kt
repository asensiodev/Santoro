package com.asensiodev.core.domain.model

data class CrewMember(
    val id: Int,
    val name: String,
    val role: CrewRole,
)
