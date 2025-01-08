package com.asensiodev.feature.moviedetail.impl.presentation.mapper

import com.asensiodev.core.domain.Genre
import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.ProductionCountry
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class MovieUiMapperTest {
    @Test
    fun `GIVEN a Movie WHEN toUi THEN returns expected MovieUi`() {
        val movie =
            Movie(
                id = 123,
                title = "Inception",
                posterPath = "/inception.jpg",
                overview = "A thief who steals corporate secrets through dream-sharing technology.",
                releaseDate = "2010-07-16",
                popularity = 8.3,
                voteAverage = 8.8,
                voteCount = 32000,
                genres = listOf(Genre("Action"), Genre("Sci-Fi")),
                productionCountries = listOf(ProductionCountry("USA")),
                isWatched = true,
                isInWatchlist = false,
            )

        val expectedMovieUi =
            MovieUi(
                id = 123,
                title = "Inception",
                posterPath = "https://image.tmdb.org/t/p/w500/inception.jpg",
                overview = "A thief who steals corporate secrets through dream-sharing technology.",
                releaseDate = "2010-07-16",
                popularity = 8.3,
                voteAverage = 8.8,
                voteCount = 32000,
                genres = listOf("Action", "Sci-Fi"),
                productionCountries = listOf("USA"),
                isWatched = true,
                isInWatchlist = false,
            )

        val result = movie.toUi()

        result shouldBeEqualTo expectedMovieUi
    }

    @Test
    fun `GIVEN a MovieUi WHEN toDomain THEN returns expected Movie`() {
        val movieUi =
            MovieUi(
                id = 123,
                title = "Inception",
                posterPath = "https://image.tmdb.org/t/p/w500/inception.jpg",
                overview = "A thief who steals corporate secrets through dream-sharing technology.",
                releaseDate = "2010-07-16",
                popularity = 8.3,
                voteAverage = 8.8,
                voteCount = 32000,
                genres = listOf("Action", "Sci-Fi"),
                productionCountries = listOf("USA"),
                isWatched = true,
                isInWatchlist = false,
            )

        val expectedMovie =
            Movie(
                id = 123,
                title = "Inception",
                posterPath = "https://image.tmdb.org/t/p/w500/inception.jpg",
                overview = "A thief who steals corporate secrets through dream-sharing technology.",
                releaseDate = "2010-07-16",
                popularity = 8.3,
                voteAverage = 8.8,
                voteCount = 32000,
                genres = listOf(Genre("Action"), Genre("Sci-Fi")),
                productionCountries = listOf(ProductionCountry("USA")),
                isWatched = true,
                isInWatchlist = false,
            )

        val result = movieUi.toDomain()

        result shouldBeEqualTo expectedMovie
    }
}
