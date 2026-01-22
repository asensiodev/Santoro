package com.asensiodev.core.data.mapper

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.ProductionCountry
import com.asensiodev.santoro.core.data.mapper.toDomain
import com.asensiodev.santoro.core.data.model.GenreApiModel
import com.asensiodev.santoro.core.data.model.MovieApiModel
import com.asensiodev.santoro.core.data.model.ProductionCountryApiModel
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class MovieApiMapperTest {
    @Test
    fun `GIVEN a MovieApiModel WHEN toDomain THEN returns expected Movie`() {
        val genreApiModels = listOf(GenreApiModel(1, "Action"), GenreApiModel(2, "Drama"))
        val productionCountryApiModels =
            listOf(
                ProductionCountryApiModel("USA"),
                ProductionCountryApiModel("UK"),
            )
        val movieApiModel =
            MovieApiModel(
                id = 1,
                title = "Inception",
                overview = "A mind-bending thriller",
                posterPath = "/inception.jpg",
                releaseDate = "2010-07-16",
                popularity = 99.9,
                voteAverage = 8.8,
                voteCount = 10000,
                genres = genreApiModels,
                productionCountries = productionCountryApiModels,
            )

        val expectedMovie =
            Movie(
                id = 1,
                title = "Inception",
                overview = "A mind-bending thriller",
                posterPath = "/inception.jpg",
                backdropPath = null,
                releaseDate = "2010-07-16",
                popularity = 99.9,
                voteAverage = 8.8,
                voteCount = 10000,
                genres = listOf(Genre("Action"), Genre("Drama")),
                productionCountries =
                    listOf(
                        ProductionCountry("USA"),
                        ProductionCountry("UK"),
                    ),
                isWatched = false,
                isInWatchlist = false,
            )

        val result = movieApiModel.toDomain()

        result shouldBeEqualTo expectedMovie
    }

    @Test
    fun `GIVEN a MovieApiModel with null optional fields WHEN toDomain THEN returns default values`() {
        val movieApiModel =
            MovieApiModel(
                id = 2,
                title = "Unknown",
                overview = "No overview available",
                posterPath = null,
                releaseDate = null,
                popularity = null,
                voteAverage = null,
                voteCount = null,
                genres = null,
                productionCountries = null,
            )

        val expectedMovie =
            Movie(
                id = 2,
                title = "Unknown",
                overview = "No overview available",
                posterPath = null,
                backdropPath = null,
                releaseDate = null,
                popularity = 0.0,
                voteAverage = 0.0,
                voteCount = 0,
                genres = emptyList(),
                productionCountries = emptyList(),
                isWatched = false,
                isInWatchlist = false,
            )

        val result = movieApiModel.toDomain()

        result shouldBeEqualTo expectedMovie
    }

    @Test
    fun `GIVEN a GenreApiModel WHEN toDomain THEN returns expected Genre`() {
        val genreApiModel = GenreApiModel(3, name = "Comedy")

        val expectedGenre = Genre(name = "Comedy")

        val result = genreApiModel.toDomain()

        result shouldBeEqualTo expectedGenre
    }

    @Test
    fun `GIVEN a ProductionCountryApiModel WHEN toDomain THEN returns expected ProductionCountry`() {
        val productionCountryApiModel = ProductionCountryApiModel(name = "France")

        val expectedProductionCountry = ProductionCountry(name = "France")

        val result = productionCountryApiModel.toDomain()

        result shouldBeEqualTo expectedProductionCountry
    }
}
