package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SearchMoviesByQueryAndGenreUseCaseTest {
    private val repository: SearchMoviesRepository = mockk()
    private val dispatchers = TestDispatcherProvider()

    private lateinit var useCase: SearchMoviesByQueryAndGenreUseCase

    @BeforeEach
    fun setUp() {
        useCase = SearchMoviesByQueryAndGenreUseCase(repository, dispatchers)
    }

    @Test
    fun `GIVEN success response WHEN invoke THEN returns movies filtered by genre`() =
        runTest {
            val query = "casino"
            val genreId = 18
            val page = 1
            val allMovies =
                listOf(
                    Movie(
                        id = 1,
                        title = "Casino",
                        posterPath = "/casino.jpg",
                        backdropPath = null,
                        overview = "A tale of greed, deception, money, power.",
                        releaseDate = "1995-11-22",
                        popularity = 7.8,
                        voteAverage = 8.2,
                        voteCount = 5000,
                        genres = listOf(Genre(18, "Drama")),
                        productionCountries = listOf(),
                        isWatched = false,
                        isInWatchlist = false,
                    ),
                    Movie(
                        id = 2,
                        title = "Casino Royale",
                        posterPath = "/royale.jpg",
                        backdropPath = null,
                        overview = "James Bond",
                        releaseDate = "2006-11-14",
                        popularity = 8.5,
                        voteAverage = 8.0,
                        voteCount = 10000,
                        genres = listOf(Genre(28, "Action")),
                        productionCountries = listOf(),
                        isWatched = false,
                        isInWatchlist = false,
                    ),
                )

            every {
                repository.searchMovies(query, page)
            } returns flowOf(Result.Success(allMovies))

            val result = useCase(query, genreId, page)

            val collectedResults = result.toList()
            collectedResults.size shouldBeEqualTo 1
            val firstResult = collectedResults.first()
            (firstResult as Result.Success).data.size shouldBeEqualTo 1
            firstResult.data.first().id shouldBeEqualTo 1
            firstResult.data.first().title shouldBeEqualTo "Casino"
        }
}
