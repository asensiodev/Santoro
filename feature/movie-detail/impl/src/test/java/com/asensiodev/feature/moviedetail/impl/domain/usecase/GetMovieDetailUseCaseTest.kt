package com.asensiodev.feature.moviedetail.impl.domain.usecase

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.core.testing.verifyOnce
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetMovieDetailUseCaseTest {
    private val repository: MovieDetailRepository = mockk()
    private val dispatchers = TestDispatcherProvider()

    private lateinit var useCase: GetMovieDetailUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetMovieDetailUseCase(repository, dispatchers)
    }

    @Test
    fun `GIVEN success response WHEN invoke THEN returns expected movie`() =
        runTest {
            val movieId = 550
            val expectedMovie =
                Movie(
                    id = 550,
                    title = "Fight Club",
                    overview = "A depressed man suffers from insomnia...",
                    releaseDate = "1999-10-15",
                    posterPath = null,
                    backdropPath = null,
                    popularity = 4.5,
                    voteAverage = 6.7,
                    voteCount = 1857,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = false,
                    isInWatchlist = false,
                )
            val expectedResult = Result.Success(expectedMovie)

            every { repository.getMovieDetail(movieId) } returns flowOf(expectedResult)

            val result = useCase(movieId)

            result.collect { collectedResult ->
                collectedResult shouldBeEqualTo expectedResult
            }
            verifyOnce { repository.getMovieDetail(movieId) }
        }
}
