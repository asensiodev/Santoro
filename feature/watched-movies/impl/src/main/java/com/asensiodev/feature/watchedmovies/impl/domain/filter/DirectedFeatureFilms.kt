package com.asensiodev.feature.watchedmovies.impl.domain.filter

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm
import java.time.LocalDate

internal fun directedFeatureFilms(
    crewCredits: List<PersonMovieCredit>,
    watchedByDirector: List<Movie>,
    today: LocalDate,
): List<DirectorFilm> {
    val watchedIds = watchedByDirector.map { it.id }.toSet()
    val features =
        crewCredits
            .filter { credit ->
                credit.movieId !in watchedIds &&
                    credit.isReleasedFeatureDirectedBy(today)
            }.map { credit ->
                DirectorFilm(
                    credit.movieId,
                    credit.title,
                    credit.posterPath,
                    credit.releaseDate,
                    isWatched = false,
                )
            }
    val watched =
        watchedByDirector.map { movie ->
            DirectorFilm(
                movie.id,
                movie.title,
                movie.posterPath,
                movie.releaseDate,
                isWatched = true,
            )
        }
    return (features + watched)
        .distinctBy { it.movieId }
        .sortedWith(
            compareBy<DirectorFilm>({
                it.releaseDate.isNullOrBlank()
            }, { it.releaseDate }, { it.title }),
        )
}

private fun PersonMovieCredit.isReleasedFeatureDirectedBy(today: LocalDate): Boolean {
    val released =
        releaseDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return false
    return DIRECTOR_ROLE in roles &&
        !isVideo &&
        DOCUMENTARY_GENRE_ID !in genreIds &&
        !released.isAfter(today)
}

private const val DIRECTOR_ROLE = "Director"
private const val DOCUMENTARY_GENRE_ID = 99
