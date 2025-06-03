package com.asensiodev.feature.watchedmovies.impl.domain.usecase

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.coVerifyOnce
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddWatchedMovieUseCaseTest {
    private val repository: DatabaseRepository = mockk()
    private val dispatchers = TestDispatcherProvider()

    private lateinit var useCase: AddWatchedMovieUseCase

    @BeforeEach
    fun setUp() {
        useCase = AddWatchedMovieUseCase(repository, dispatchers)
    }

    @Test
    fun `GIVEN a movie WHEN invoke THEN updates the movie state`() =
        runTest {
            val movie =
                Movie(
                    id = 1,
                    title = "Inception",
                    posterPath = "/inception.jpg",
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

            coEvery { repository.updateMovieState(movie) } returns Result.Success(true)

            useCase(movie)

            coVerifyOnce { repository.updateMovieState(movie) }
        }
}
