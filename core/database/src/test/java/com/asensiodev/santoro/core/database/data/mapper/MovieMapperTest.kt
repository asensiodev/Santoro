package com.asensiodev.santoro.core.database.data.mapper

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.google.gson.Gson
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class MovieMapperTest {
    @Test
    fun `GIVEN serialized genres with invalid names WHEN toGenres THEN filters invalid entries`() {
        val result = serializedGenres().toGenres()

        result shouldBeEqualTo listOf(Genre(id = 2, name = "Action"))
    }

    @Test
    fun `GIVEN movie with invalid genre names WHEN toEntity THEN only valid genres are persisted`() {
        val movie =
            Movie(
                id = 1,
                title = "Inception",
                overview = "Overview",
                posterPath = null,
                backdropPath = null,
                releaseDate = "2010-07-16",
                popularity = 8.0,
                voteAverage = 8.8,
                voteCount = 100,
                genres = Gson().fromJson(serializedGenres(), Array<Genre>::class.java).toList(),
                productionCountries = emptyList(),
                isWatched = false,
                isInWatchlist = true,
            )

        val result = movie.toEntity().genres.toGenres()

        result shouldBeEqualTo listOf(Genre(id = 2, name = "Action"))
    }
}

private fun serializedGenres() =
    """
    [
      {"id":1,"name":null},
      {"id":2,"name":"Action"},
      {"id":3,"name":""},
      {"id":4,"name":"null"}
    ]
    """.trimIndent()
