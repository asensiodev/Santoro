package com.asensiodev.core.data.mapper

import com.asensiodev.core.domain.model.CastMember
import com.asensiodev.core.domain.model.Director
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.ProductionCountry
import com.asensiodev.santoro.core.data.mapper.toDomain
import com.asensiodev.santoro.core.data.model.CastMemberApiModel
import com.asensiodev.santoro.core.data.model.CreditsApiModel
import com.asensiodev.santoro.core.data.model.CrewMemberApiModel
import com.asensiodev.santoro.core.data.model.GenreApiModel
import com.asensiodev.santoro.core.data.model.MovieApiModel
import com.asensiodev.santoro.core.data.model.ProductionCountryApiModel
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class MovieApiMapperTest {
    @Test
    fun `GIVEN credits with co-directors WHEN toDomain THEN resolves each director once in credit order`() {
        val crew =
            listOf(
                CrewMemberApiModel(id = 1223, name = "Joel Coen", job = "Director"),
                CrewMemberApiModel(id = 1223, name = "Joel Coen", job = "Director"),
                CrewMemberApiModel(id = 1224, name = "Ethan Coen", job = "Director"),
                CrewMemberApiModel(id = 7, name = "Roger Deakins", job = "Director of Photography"),
            )

        val result = creditsMovie(CreditsApiModel(cast = null, crew = crew)).toDomain()

        result.directors shouldBeEqualTo
            listOf(Director(id = 1223, name = "Joel Coen"), Director(id = 1224, name = "Ethan Coen"))
    }

    @Test
    fun `GIVEN credits without a director WHEN toDomain THEN directors are resolved empty`() {
        val crew = listOf(CrewMemberApiModel(id = 7, name = "Roger Deakins", job = "Director of Photography"))

        val result = creditsMovie(CreditsApiModel(cast = emptyList(), crew = crew)).toDomain()

        result.directors shouldBeEqualTo emptyList()
    }

    @Test
    fun `GIVEN credits without crew WHEN toDomain THEN directors are resolved empty`() {
        val result = creditsMovie(CreditsApiModel(cast = emptyList(), crew = null)).toDomain()

        result.directors shouldBeEqualTo emptyList()
    }

    @Test
    fun `GIVEN no credits WHEN toDomain THEN directors stay unresolved`() {
        val result = creditsMovie(credits = null).toDomain()

        result.directors shouldBeEqualTo null
    }

    private fun creditsMovie(credits: CreditsApiModel?) =
        MovieApiModel(
            id = 115,
            title = "The Big Lebowski",
            overview = "Overview",
            posterPath = null,
            releaseDate = "1998-03-06",
            popularity = null,
            voteAverage = null,
            voteCount = null,
            genres = null,
            genreIds = null,
            productionCountries = null,
            runtime = null,
            tagline = null,
            backdropPath = null,
            credits = credits,
        )

    @Test
    fun `GIVEN a CastMemberApiModel WHEN toDomain THEN preserves credit identity`() {
        val castMember =
            CastMemberApiModel(
                id = 1,
                creditId = "credit-1",
                name = "Leo",
                character = "Cobb",
                profilePath = "/leo.jpg",
            )

        castMember.toDomain() shouldBeEqualTo
            CastMember(
                id = 1,
                creditId = "credit-1",
                name = "Leo",
                character = "Cobb",
                profilePath = "/leo.jpg",
            )
    }

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
                genreIds = null,
                productionCountries = productionCountryApiModels,
                runtime = 148,
                tagline = "Your mind is the scene of the crime.",
                backdropPath = null,
                credits = null,
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
                genres = listOf(Genre(id = 1, name = "Action"), Genre(id = 2, name = "Drama")),
                genreIds = listOf(1, 2),
                productionCountries =
                    listOf(
                        ProductionCountry("USA"),
                        ProductionCountry("UK"),
                    ),
                cast = emptyList(),
                crew = emptyList(),
                runtime = 148,
                tagline = "Your mind is the scene of the crime.",
                isWatched = false,
                isInWatchlist = false,
            )

        val result = movieApiModel.toDomain()

        result shouldBeEqualTo expectedMovie
    }

    @Test
    fun `GIVEN a MovieApiModel with null optional fields WHEN toDomain THEN returns expected Movie with defaults`() {
        val movieApiModel =
            MovieApiModel(
                id = 2,
                title = "Unknown",
                overview = "No description",
                posterPath = null,
                releaseDate = null,
                popularity = null,
                voteAverage = null,
                voteCount = null,
                genres = null,
                genreIds = null,
                productionCountries = null,
                runtime = null,
                tagline = null,
                backdropPath = null,
                credits = null,
            )

        val expectedMovie =
            Movie(
                id = 2,
                title = "Unknown",
                overview = "No description",
                posterPath = null,
                backdropPath = null,
                releaseDate = null,
                popularity = 0.0,
                voteAverage = 0.0,
                voteCount = 0,
                genres = emptyList(),
                genreIds = emptyList(),
                productionCountries = emptyList(),
                cast = emptyList(),
                crew = emptyList(),
                runtime = null,
                tagline = null,
                isWatched = false,
                isInWatchlist = false,
            )

        val result = movieApiModel.toDomain()

        result shouldBeEqualTo expectedMovie
    }

    @Test
    fun `GIVEN a GenreApiModel WHEN toDomain THEN returns expected Genre`() {
        val genreApiModel = GenreApiModel(3, name = "Comedy")

        val expectedGenre = Genre(id = 3, name = "Comedy")

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
