package com.asensiodev.feature.watchedmovies.impl.presentation.mapper

import com.asensiodev.core.domain.model.Director
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupLabel
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class WatchedGroupMapperTest {
    @Test
    fun `GIVEN movies watched in different months WHEN grouped by date watched THEN keeps watched order`() {
        val march = movie(1, "Dune", watchedAt = MARCH_2026)
        val earlyMarch = movie(2, "Arrival", watchedAt = MARCH_2026 - DAY)
        val january = movie(3, "Sicario", watchedAt = JANUARY_2026)
        val unknown = movie(4, "Incendies", watchedAt = null)

        val groups = listOf(march, earlyMarch, january, unknown).toWatchedGroups(WatchedGrouping.DATE_WATCHED)

        groups.map { it.label } shouldBeEqualTo
            listOf(
                WatchedGroupLabel.Text(march.toUi().watchedDate.orEmpty()),
                WatchedGroupLabel.Text(january.toUi().watchedDate.orEmpty()),
                WatchedGroupLabel.UnknownDate,
            )
        groups.first().movies.map { it.id } shouldBeEqualTo listOf(1, 2)
    }

    @Test
    fun `GIVEN directors WHEN grouped by director THEN orders by count then name with unknown last`() {
        val movies =
            listOf(
                movie(1, "Dune", directors = listOf(VILLENEUVE)),
                movie(2, "Memento", directors = listOf(NOLAN)),
                movie(3, "Arrival", directors = listOf(VILLENEUVE)),
                movie(4, "Fargo", directors = listOf(JOEL_COEN, ETHAN_COEN)),
                movie(5, "Unresolved", directors = null),
                movie(6, "Documentary", directors = emptyList()),
            )

        val groups = movies.toWatchedGroups(WatchedGrouping.DIRECTOR)

        groups.map { it.label } shouldBeEqualTo
            listOf(
                WatchedGroupLabel.Text("Denis Villeneuve"),
                WatchedGroupLabel.Text("Christopher Nolan"),
                WatchedGroupLabel.Text("Ethan Coen"),
                WatchedGroupLabel.Text("Joel Coen"),
                WatchedGroupLabel.UnknownDirector,
            )
        groups.map { it.directorId } shouldBeEqualTo listOf(137427, 525, 1224, 1223, null)
        groups.last().movies.map { it.id } shouldBeEqualTo listOf(6, 5)
    }

    @Test
    fun `GIVEN a group WHEN grouped by director THEN movies follow release date with undated last`() {
        val movies =
            listOf(
                movie(1, "Dune", releaseDate = "2021-09-15", directors = listOf(VILLENEUVE)),
                movie(2, "Undated", releaseDate = null, directors = listOf(VILLENEUVE)),
                movie(3, "Arrival", releaseDate = "2016-11-11", directors = listOf(VILLENEUVE)),
                movie(4, "Blade Runner 2049", releaseDate = "2016-11-11", directors = listOf(VILLENEUVE)),
            )

        val group = movies.toWatchedGroups(WatchedGrouping.DIRECTOR).single()

        group.movies.map { it.title } shouldBeEqualTo listOf("Arrival", "Blade Runner 2049", "Dune", "Undated")
    }

    @Test
    fun `GIVEN genres WHEN grouped by genre THEN each genre lists its movies and no genre is last`() {
        val movies =
            listOf(
                movie(1, "Dune", genres = listOf(SCIENCE_FICTION, ADVENTURE)),
                movie(2, "Arrival", genres = listOf(SCIENCE_FICTION, DRAMA)),
                movie(3, "Incendies", genres = listOf(DRAMA)),
                movie(4, "Untagged", genres = emptyList()),
            )

        val groups = movies.toWatchedGroups(WatchedGrouping.GENRE)

        groups.map { it.label } shouldBeEqualTo
            listOf(
                WatchedGroupLabel.Text("Drama"),
                WatchedGroupLabel.Text("Science Fiction"),
                WatchedGroupLabel.Text("Adventure"),
                WatchedGroupLabel.NoGenre,
            )
        groups.map { group -> group.movies.map { it.id } } shouldBeEqualTo
            listOf(listOf(2, 3), listOf(2, 1), listOf(1), listOf(4))
    }

    @Test
    fun `GIVEN release dates WHEN grouped by decade THEN newest decade first and undated last`() {
        val movies =
            listOf(
                movie(1, "Goodfellas", releaseDate = "1990-09-12"),
                movie(2, "Dune", releaseDate = "2021-09-15"),
                movie(3, "Taxi Driver", releaseDate = "1976-02-08"),
                movie(4, "Casino", releaseDate = "1995-11-22"),
                movie(5, "Broken date", releaseDate = "unknown"),
            )

        val groups = movies.toWatchedGroups(WatchedGrouping.DECADE)

        groups.map { it.label } shouldBeEqualTo
            listOf(
                WatchedGroupLabel.Decade(2020),
                WatchedGroupLabel.Decade(1990),
                WatchedGroupLabel.Decade(1970),
                WatchedGroupLabel.Undated,
            )
        groups[1].movies.map { it.id } shouldBeEqualTo listOf(1, 4)
    }

    @Test
    fun `GIVEN co-directed movie WHEN grouped by director THEN keys stay unique per group`() {
        val fargo = movie(4, "Fargo", directors = listOf(JOEL_COEN, ETHAN_COEN))

        val groups = listOf(fargo).toWatchedGroups(WatchedGrouping.DIRECTOR)

        groups.map { it.key }.distinct().size shouldBeEqualTo 2
    }

    @Test
    fun `GIVEN movie with metadata WHEN mapped to ui THEN exposes release year and directors`() {
        val fargo = movie(4, "Fargo", releaseDate = "1996-03-08", directors = listOf(JOEL_COEN, ETHAN_COEN))

        val ui = fargo.toUi()

        ui.releaseYear shouldBeEqualTo "1996"
        ui.directors shouldBeEqualTo "Joel Coen, Ethan Coen"
    }

    private fun movie(
        id: Int,
        title: String,
        releaseDate: String? = "2020-01-01",
        genres: List<Genre> = emptyList(),
        directors: List<Director>? = null,
        watchedAt: Long? = JANUARY_2026,
    ) = Movie(
        id = id,
        title = title,
        overview = "",
        posterPath = null,
        backdropPath = null,
        releaseDate = releaseDate,
        popularity = 0.0,
        voteAverage = 0.0,
        voteCount = 0,
        genres = genres,
        productionCountries = emptyList(),
        directors = directors,
        isWatched = true,
        isInWatchlist = false,
        watchedAt = watchedAt,
    )

    private companion object {
        const val DAY = 86_400_000L
        const val JANUARY_2026 = 1_768_000_000_000L
        const val MARCH_2026 = 1_773_500_000_000L
        val VILLENEUVE = Director(137427, "Denis Villeneuve")
        val NOLAN = Director(525, "Christopher Nolan")
        val JOEL_COEN = Director(1223, "Joel Coen")
        val ETHAN_COEN = Director(1224, "Ethan Coen")
        val SCIENCE_FICTION = Genre(878, "Science Fiction")
        val ADVENTURE = Genre(12, "Adventure")
        val DRAMA = Genre(18, "Drama")
    }
}
