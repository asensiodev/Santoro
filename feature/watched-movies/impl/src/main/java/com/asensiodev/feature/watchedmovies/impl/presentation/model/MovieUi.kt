package com.asensiodev.feature.watchedmovies.impl.presentation.model

data class MovieUi(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val watchedDate: String?,
    val releaseYear: String? = null,
    val directors: String? = null,
)
