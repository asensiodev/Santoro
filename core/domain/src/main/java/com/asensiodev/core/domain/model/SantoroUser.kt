package com.asensiodev.core.domain.model

data class SantoroUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isAnonymous: Boolean,
)
