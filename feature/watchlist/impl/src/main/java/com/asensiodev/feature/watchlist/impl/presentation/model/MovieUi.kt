package com.asensiodev.feature.watchlist.impl.presentation.model

data class MovieUi(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val releaseYear: String?,
    val genres: String?,
    val rating: Double,
)
