package com.asensiodev.feature.watchlist.impl.presentation.mapper

import com.asensiodev.core.domain.MovieDetail
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi

internal fun MovieDetail.toUi(): MovieUi =
    MovieUi(
        id = id,
        title = title,
        posterPath = posterPath?.let { BASE_POSTER_URL + it },
    )

internal fun List<MovieDetail>.toUiList(): List<MovieUi> = this.map { it.toUi() }

private const val BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500"
