package com.asensiodev.santoro.core.database.data.mapper

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.MovieSyncData
import com.asensiodev.santoro.core.database.data.model.MovieEntity
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

    @Test
    fun `GIVEN sync movie WHEN toEntity THEN remote fields and content defaults are mapped`() {
        val syncMovie =
            MovieSyncData(
                movieId = 601,
                title = "Synced",
                posterPath = "/synced.jpg",
                genres = listOf(Genre(id = 18, name = "Drama")),
                runtime = 140,
                isWatched = true,
                isInWatchlist = false,
                watchedAt = 900L,
                updatedAt = 1_000L,
            )

        syncMovie.toEntity() shouldBeEqualTo
            MovieEntity(
                id = 601,
                title = "Synced",
                overview = "",
                posterPath = "/synced.jpg",
                releaseDate = null,
                popularity = 0.0,
                voteAverage = 0.0,
                voteCount = 0,
                genres = "[{\"id\":18,\"name\":\"Drama\"}]",
                productionCountries = "",
                tagline = null,
                runtime = 140,
                isWatched = true,
                isInWatchlist = false,
                watchedAt = 900L,
                updatedAt = 1_000L,
            )
    }

    @Test
    fun `GIVEN movie entity WHEN toSyncData THEN remote metadata and state are mapped`() {
        val entity =
            MovieEntity(
                id = 602,
                title = "Upload",
                overview = "Overview",
                posterPath = "/upload.jpg",
                releaseDate = "2026-01-01",
                popularity = 7.5,
                voteAverage = 8.0,
                voteCount = 50,
                genres = "[{\"id\":28,\"name\":\"Action\"}]",
                productionCountries = "[]",
                tagline = "Tagline",
                runtime = 120,
                isWatched = false,
                isInWatchlist = true,
                watchedAt = null,
                updatedAt = 2_000L,
            )

        entity.toSyncData() shouldBeEqualTo
            MovieSyncData(
                movieId = 602,
                title = "Upload",
                posterPath = "/upload.jpg",
                genres = listOf(Genre(id = 28, name = "Action")),
                runtime = 120,
                isWatched = false,
                isInWatchlist = true,
                watchedAt = null,
                updatedAt = 2_000L,
            )
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
