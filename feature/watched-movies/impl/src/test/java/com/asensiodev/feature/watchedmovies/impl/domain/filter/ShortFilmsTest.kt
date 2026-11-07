package com.asensiodev.feature.watchedmovies.impl.domain.filter

import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ShortFilmsTest {
    @Test
    fun `GIVEN runtimes around the short limit WHEN splitting THEN 40 minutes or less are shorts`() {
        val films = listOf(film(1), film(2), film(3), film(4))

        val filmography = films.splitShorts(mapOf(1 to 40, 2 to 41, 3 to 9))

        filmography shouldBeEqualTo
            DirectorFilmography(
                features = listOf(film(2), film(4)),
                shorts = listOf(film(1), film(3)),
            )
    }

    @Test
    fun `GIVEN a watched short WHEN splitting THEN it stays watched in the shorts`() {
        val watchedShort = film(5, isWatched = true)

        listOf(film(6), watchedShort).splitShorts(mapOf(5 to 29, 6 to 120)).shorts shouldBeEqualTo listOf(watchedShort)
    }

    private fun film(
        id: Int,
        isWatched: Boolean = false,
    ) = DirectorFilm(id, "Film $id", null, "2000-01-0$id", isWatched)
}
