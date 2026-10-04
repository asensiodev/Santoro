package com.asensiodev.core.domain.model

data class Person(
    val id: Int,
    val name: String,
    val biography: String,
    val profilePath: String?,
    val birthday: String?,
    val deathday: String?,
    val birthplace: String?,
    val knownForDepartment: String? = null,
)
