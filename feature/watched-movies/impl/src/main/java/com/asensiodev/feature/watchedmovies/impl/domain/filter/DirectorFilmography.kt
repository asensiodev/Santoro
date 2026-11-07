package com.asensiodev.feature.watchedmovies.impl.domain.filter

import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm

internal data class DirectorFilmography(
    val features: List<DirectorFilm>,
    val shorts: List<DirectorFilm>,
)

internal fun List<DirectorFilm>.splitShorts(runtimes: Map<Int, Int>): DirectorFilmography {
    val (shorts, features) =
        partition { film ->
            runtimes[film.movieId]?.let {
                it <=
                    SHORT_MAX_MINUTES
            } ==
                true
        }
    return DirectorFilmography(features = features, shorts = shorts)
}

private const val SHORT_MAX_MINUTES = 40
