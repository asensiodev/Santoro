package com.asensiodev.feature.searchmovies.impl.data.repository

import app.cash.turbine.test
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultMovieLibraryStatusRepositoryTest {
    private val databaseRepository: DatabaseRepository = mockk()
    private lateinit var repository: DefaultMovieLibraryStatusRepository

    @BeforeEach
    fun setUp() {
        repository = DefaultMovieLibraryStatusRepository(databaseRepository)
    }

    @Test
    fun `GIVEN watched movies WHEN observing watched IDs THEN emits unique IDs`() =
        runTest {
            every { databaseRepository.getWatchedMovies() } returns
                flowOf(Result.success(listOf(movie(1), movie(2), movie(1))))

            repository.observeWatchedMovieIds().test {
                awaitItem() shouldBeEqualTo Result.success(setOf(1, 2))
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN watchlist movies WHEN observing watchlist IDs THEN emits unique IDs`() =
        runTest {
            every { databaseRepository.getWatchlistMovies() } returns
                flowOf(Result.success(listOf(movie(3), movie(4), movie(3))))

            repository.observeWatchlistMovieIds().test {
                awaitItem() shouldBeEqualTo Result.success(setOf(3, 4))
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN watched failure and recovery WHEN observing THEN preserves both emissions`() =
        runTest {
            val exception = IllegalStateException()
            val source = MutableSharedFlow<Result<List<Movie>>>()
            every { databaseRepository.getWatchedMovies() } returns source

            repository.observeWatchedMovieIds().test {
                source.emit(Result.failure(exception))
                awaitItem() shouldBeEqualTo Result.failure(exception)
                source.emit(Result.success(listOf(movie(5))))
                awaitItem() shouldBeEqualTo Result.success(setOf(5))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN watchlist failure and recovery WHEN observing THEN preserves both emissions`() =
        runTest {
            val exception = IllegalStateException()
            val source = MutableSharedFlow<Result<List<Movie>>>()
            every { databaseRepository.getWatchlistMovies() } returns source

            repository.observeWatchlistMovieIds().test {
                source.emit(Result.failure(exception))
                awaitItem() shouldBeEqualTo Result.failure(exception)
                source.emit(Result.success(listOf(movie(6))))
                awaitItem() shouldBeEqualTo Result.success(setOf(6))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN wrapped cancellation WHEN observing THEN cancellation propagates`() =
        runTest {
            every { databaseRepository.getWatchedMovies() } returns
                flowOf(Result.failure(CancellationException()))

            repository.observeWatchedMovieIds().test {
                awaitError().shouldBeInstanceOf<CancellationException>()
            }
        }

    @Test
    fun `GIVEN upstream cancellation WHEN observing THEN cancellation propagates`() =
        runTest {
            every { databaseRepository.getWatchlistMovies() } returns
                flow { throw CancellationException() }

            repository.observeWatchlistMovieIds().test {
                awaitError().shouldBeInstanceOf<CancellationException>()
            }
        }

    private fun movie(id: Int) =
        Movie(
            id = id,
            title = "Movie $id",
            overview = "Overview",
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
}
