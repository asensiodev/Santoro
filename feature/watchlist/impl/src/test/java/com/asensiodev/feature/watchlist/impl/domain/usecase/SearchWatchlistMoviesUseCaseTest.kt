package com.asensiodev.feature.watchlist.impl.domain.usecase

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SearchWatchlistMoviesUseCaseTest {
    private val repository: DatabaseRepository = mockk()
    private val dispatchers = TestDispatcherProvider()

    private lateinit var useCase: SearchWatchlistMoviesUseCase

    @BeforeEach
    fun setUp() {
        useCase = SearchWatchlistMoviesUseCase(repository, dispatchers)
    }

    @Test
    fun `GIVEN success response WHEN invoke THEN returns expected movies`() =
        runTest {
            val query = "Inception"
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
                        isWatched = false,
                        isInWatchlist = true,
                    ),
                )
            val expectedResult = Result.Success(movies)

            every { repository.searchWatchlistMoviesByTitle(query) } returns flowOf(expectedResult)

            val result = useCase(query)

            result.collect { collectedResult ->
                collectedResult shouldBeEqualTo expectedResult
            }
        }
}
