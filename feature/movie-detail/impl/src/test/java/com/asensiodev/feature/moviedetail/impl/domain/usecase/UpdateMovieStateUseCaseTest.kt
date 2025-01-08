package com.asensiodev.feature.moviedetail.impl.domain.usecase

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import com.asensiodev.core.testing.coVerifyOnce
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateMovieStateUseCaseTest {
    private val repository: MovieDetailRepository = mockk()

    private lateinit var useCase: UpdateMovieStateUseCase

    @BeforeEach
    fun setUp() {
        useCase = UpdateMovieStateUseCase(repository)
    }

    @Test
    fun `GIVEN success movie update WHEN invoke THEN returns true`() =
        runTest {
            val movie =
                Movie(
                    id = 123,
                    title = "Inception",
                    overview = "A thief who steals corporate secrets through the use of dream-sharing technology.",
                    releaseDate = "2010-07-16",
                    posterPath = null,
                    popularity = 8.3,
                    voteAverage = 8.8,
                    voteCount = 32000,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = true,
                    isInWatchlist = false,
                )
            val expectedResult = Result.Success(true)

            coEvery { repository.updateMovieState(movie) } returns expectedResult

            val result = useCase(movie)

            result shouldBeEqualTo expectedResult
            coVerifyOnce { repository.updateMovieState(movie) }
        }
}
