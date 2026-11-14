package com.asensiodev.feature.moviedetail.impl.domain.usecase

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
import java.io.IOException

class HasSavedMoviesUseCaseTest {
    private val repository: DatabaseRepository = mockk()
    private val useCase = HasSavedMoviesUseCase(repository, TestDispatcherProvider())

    @BeforeEach
    fun setUp() {
        every { repository.getWatchedMovies() } returns flowOf(Result.success(emptyList()))
        every { repository.getWatchlistMovies() } returns flowOf(Result.success(emptyList()))
    }

    @Test
    fun `GIVEN no saved movies WHEN invoked THEN returns false`() =
        runTest {
            useCase() shouldBeEqualTo false
        }

    @Test
    fun `GIVEN a watched movie WHEN invoked THEN returns true`() =
        runTest {
            every { repository.getWatchedMovies() } returns flowOf(Result.success(listOf(movie())))

            useCase() shouldBeEqualTo true
        }

    @Test
    fun `GIVEN a watchlist movie WHEN invoked THEN returns true`() =
        runTest {
            every { repository.getWatchlistMovies() } returns flowOf(Result.success(listOf(movie())))

            useCase() shouldBeEqualTo true
        }

    @Test
    fun `GIVEN both reads fail WHEN invoked THEN returns false`() =
        runTest {
            every { repository.getWatchedMovies() } returns flowOf(Result.failure(IOException("disk")))
            every { repository.getWatchlistMovies() } returns flowOf(Result.failure(IOException("disk")))

            useCase() shouldBeEqualTo false
        }

    private fun movie() =
        Movie(
            id = 1,
            title = "Arrival",
            overview = "",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            popularity = 0.0,
            voteAverage = 0.0,
            voteCount = 0,
            genres = emptyList(),
            productionCountries = emptyList(),
            isWatched = true,
            isInWatchlist = false,
        )
}
