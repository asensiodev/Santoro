package com.asensiodev.feature.watchedmovies.impl.domain.filter

import com.asensiodev.core.domain.model.Director
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DirectedFeatureFilmsTest {
    @Test
    fun `GIVEN crew credits WHEN filtering THEN keeps only released directed feature films`() {
        val credits =
            listOf(
                credit(1, "Feature", "2010-07-16"),
                credit(2, "Written only", "2011-01-01", roles = listOf("Writer")),
                credit(3, "Concert", "2012-01-01", isVideo = true),
                credit(4, "Documentary", "2013-01-01", genreIds = listOf(DOCUMENTARY, 18)),
                credit(5, "Upcoming", "2027-01-01"),
                credit(6, "Undated", null),
            )

        directedFeatureFilms(credits, emptyList(), TODAY).map { it.movieId } shouldBeEqualTo listOf(1)
    }

    @Test
    fun `GIVEN watched films excluded by the filter WHEN filtering THEN they are kept as watched`() {
        val credits =
            listOf(
                credit(4, "Documentary", "2013-01-01", genreIds = listOf(DOCUMENTARY)),
                credit(7, "Short", "2005-01-01", isVideo = true),
            )
        val watched = listOf(watched(4, "Documentary", "2013-01-01"), watched(8, "Not in credits", "2001-05-05"))

        directedFeatureFilms(credits, watched, TODAY) shouldBeEqualTo
            listOf(
                DirectorFilm(8, "Not in credits", "/8.jpg", "2001-05-05", isWatched = true),
                DirectorFilm(4, "Documentary", "/4.jpg", "2013-01-01", isWatched = true),
            )
    }

    @Test
    fun `GIVEN a watched feature film in the credits WHEN filtering THEN it appears once marked as watched`() {
        val credits = listOf(credit(1, "Feature", "2010-07-16"), credit(2, "Other", "2014-11-05"))

        directedFeatureFilms(credits, listOf(watched(1, "Feature", "2010-07-16")), TODAY) shouldBeEqualTo
            listOf(
                DirectorFilm(1, "Feature", "/1.jpg", "2010-07-16", isWatched = true),
                DirectorFilm(2, "Other", "/2.jpg", "2014-11-05", isWatched = false),
            )
    }

    @Test
    fun `GIVEN same-date and undated films WHEN filtering THEN orders by date then title with undated last`() {
        val credits =
            listOf(credit(2, "Beta", "2010-01-01"), credit(1, "Alpha", "2010-01-01"), credit(3, "Old", "1999-01-01"))
        val watched = listOf(watched(9, "Undated watched", null))

        directedFeatureFilms(credits, watched, TODAY).map { it.title } shouldBeEqualTo
            listOf("Old", "Alpha", "Beta", "Undated watched")
    }

    @Test
    fun `GIVEN a film released today WHEN filtering THEN it is included`() {
        directedFeatureFilms(listOf(credit(1, "Today", TODAY.toString())), emptyList(), TODAY)
            .map { it.movieId } shouldBeEqualTo listOf(1)
    }

    private fun credit(
        id: Int,
        title: String,
        releaseDate: String?,
        roles: List<String> = listOf("Director"),
        isVideo: Boolean = false,
        genreIds: List<Int> = listOf(18),
    ) = PersonMovieCredit(id, title, "/$id.jpg", releaseDate, roles, isVideo, genreIds)

    private fun watched(
        id: Int,
        title: String,
        releaseDate: String?,
    ) = Movie(
        id = id,
        title = title,
        overview = "",
        posterPath = "/$id.jpg",
        backdropPath = null,
        releaseDate = releaseDate,
        popularity = 0.0,
        voteAverage = 0.0,
        voteCount = 0,
        genres = emptyList(),
        productionCountries = emptyList(),
        directors = listOf(Director(DIRECTOR_ID, "Christopher Nolan")),
        isWatched = true,
        isInWatchlist = false,
    )

    private companion object {
        const val DOCUMENTARY = 99
        const val DIRECTOR_ID = 525
        val TODAY: LocalDate = LocalDate.of(2026, 11, 4)
    }
}
