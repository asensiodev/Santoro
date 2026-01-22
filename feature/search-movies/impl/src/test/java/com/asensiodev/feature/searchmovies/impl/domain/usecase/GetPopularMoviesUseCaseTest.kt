package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetPopularMoviesUseCaseTest {
    private val repository: SearchMoviesRepository = mockk()
    private val dispatchers = TestDispatcherProvider()

    private lateinit var useCase: GetPopularMoviesUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetPopularMoviesUseCase(repository, dispatchers)
    }

    @Test
    fun `GIVEN success response WHEN invoke THEN returns expected movies`() =
        runTest {
            val page = 1
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
                        isInWatchlist = false,
                    ),
                )
            val expectedResult = Result.Success(movies)

            every { repository.getPopularMovies(page) } returns flowOf(expectedResult)

            val result = useCase(page)

            result.collect { collectedResult ->
                collectedResult shouldBeEqualTo expectedResult
            }
        }
}
