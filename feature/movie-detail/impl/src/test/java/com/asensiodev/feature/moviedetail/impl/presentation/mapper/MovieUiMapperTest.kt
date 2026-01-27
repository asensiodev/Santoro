package com.asensiodev.feature.moviedetail.impl.presentation.mapper

import com.asensiodev.core.domain.model.CastMember
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.ProductionCountry
import com.asensiodev.feature.moviedetail.impl.presentation.model.CastMemberUi
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
                backdropPath = null,
                overview = "A thief who steals corporate secrets through dream-sharing technology.",
                releaseDate = "2010-07-16",
                popularity = 8.3,
                voteAverage = 8.8,
                voteCount = 32000,
                genres = listOf(Genre("Action"), Genre("Sci-Fi")),
                productionCountries = listOf(ProductionCountry("USA")),
                cast = listOf(CastMember(1, "Leo", "Cobb", "/leo.jpg")),
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
                cast = listOf(CastMemberUi(1, "Leo", "Cobb", "https://image.tmdb.org/t/p/w185/leo.jpg")),
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
                cast = listOf(CastMemberUi(1, "Leo", "Cobb", "https://image.tmdb.org/t/p/w185/leo.jpg")),
                isWatched = true,
                isInWatchlist = false,
            )

        val expectedMovie =
            Movie(
                id = 123,
                title = "Inception",
                posterPath = "/inception.jpg",
                backdropPath = null,
                overview = "A thief who steals corporate secrets through dream-sharing technology.",
                releaseDate = "2010-07-16",
                popularity = 8.3,
                voteAverage = 8.8,
                voteCount = 32000,
                genres = listOf(Genre("Action"), Genre("Sci-Fi")),
                productionCountries = listOf(ProductionCountry("USA")),
                cast = listOf(CastMember(1, "Leo", "Cobb", "/leo.jpg")),
                isWatched = true,
                isInWatchlist = false,
            )

        val result = movieUi.toDomain()

        result shouldBeEqualTo expectedMovie
    }
}
