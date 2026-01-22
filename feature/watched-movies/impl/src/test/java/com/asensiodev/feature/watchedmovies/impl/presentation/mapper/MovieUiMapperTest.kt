package com.asensiodev.feature.watchedmovies.impl.presentation.mapper

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class MovieUiMapperTest {
    @Test
    fun `GIVEN a Movie WHEN toUi THEN returns expected MovieUi`() {
        val movie =
            Movie(
                id = 1,
                title = "Inception",
                posterPath = "/inception.jpg",
                backdropPath = null,
                overview = "A thief who steals corporate secrets.",
                releaseDate = "2010-07-16",
                popularity = 8.3,
                voteAverage = 8.8,
                voteCount = 32000,
                genres = listOf(),
                productionCountries = listOf(),
                isWatched = true,
                isInWatchlist = false,
            )

        val expectedMovieUi =
            MovieUi(
                id = 1,
                title = "Inception",
                posterPath = "https://image.tmdb.org/t/p/w500/inception.jpg",
            )

        val result = movie.toUi()

        result shouldBeEqualTo expectedMovieUi
    }

    @Test
    fun `GIVEN a list of Movie WHEN toUiList THEN returns expected list of MovieUi`() {
        val movies =
            listOf(
                Movie(
                    id = 1,
                    title = "Inception",
                    posterPath = "/inception.jpg",
                    backdropPath = null,
                    overview = "A thief who steals corporate secrets.",
                    releaseDate = "2010-07-16",
                    popularity = 8.3,
                    voteAverage = 8.8,
                    voteCount = 32000,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = true,
                    isInWatchlist = false,
                ),
                Movie(
                    id = 2,
                    title = "The Dark Knight",
                    posterPath = "/dark_knight.jpg",
                    backdropPath = null,
                    overview = "Batman faces the Joker in Gotham City.",
                    releaseDate = "2008-07-18",
                    popularity = 9.0,
                    voteAverage = 9.1,
                    voteCount = 41000,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = false,
                    isInWatchlist = false,
                ),
            )

        val expectedMoviesUi =
            listOf(
                MovieUi(
                    id = 1,
                    title = "Inception",
                    posterPath = "https://image.tmdb.org/t/p/w500/inception.jpg",
                ),
                MovieUi(
                    id = 2,
                    title = "The Dark Knight",
                    posterPath = "https://image.tmdb.org/t/p/w500/dark_knight.jpg",
                ),
            )

        val result = movies.toUiList()

        result shouldBeEqualTo expectedMoviesUi
    }
}
