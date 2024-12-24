package com.asensiodev.library.network.impl.data.mapper

import com.asensiodev.core.domain.Movie
import com.asensiodev.library.network.impl.data.model.MovieApiModel

internal fun MovieApiModel.toDomain() =
    Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        releaseDate = releaseDate,
        popularity = popularity ?: 0.0,
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
    )
