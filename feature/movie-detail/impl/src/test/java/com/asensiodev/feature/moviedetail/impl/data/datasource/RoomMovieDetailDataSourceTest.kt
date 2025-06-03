package com.asensiodev.feature.moviedetail.impl.data.datasource

import app.cash.turbine.test
import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomMovieDetailDataSourceTest {
    private val databaseRepository: DatabaseRepository = mockk()

    private lateinit var dataSource: RoomMovieDetailDataSource

    @BeforeEach
    fun setUp() {
        dataSource = RoomMovieDetailDataSource(databaseRepository)
    }

    @Test
    fun `GIVEN database success WHEN getMovieDetail THEN returns expected movie`() =
        runTest {
            val movieId = 1
            val movie = mockk<Movie>()

            coEvery { databaseRepository.getMovieById(movieId) } returns Result.Success(movie)

            dataSource.getMovieDetail(movieId).test {
                awaitItem() shouldBeEqualTo Result.Loading
                awaitItem() shouldBeEqualTo Result.Success(movie)
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN database error WHEN getMovieDetail THEN returns error`() =
        runTest {
            val movieId = 1
            val exception = RuntimeException("DB error")

            coEvery { databaseRepository.getMovieById(movieId) } returns Result.Error(exception)

            dataSource.getMovieDetail(movieId).test {
                awaitItem() shouldBeEqualTo Result.Loading
                val error = awaitItem() as Result.Error
                error.exception shouldBeEqualTo exception
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN movie update succeeds WHEN updateMovieState THEN returns success`() =
        runTest {
            val movie = mockk<Movie>()

            coEvery { databaseRepository.updateMovieState(movie) } returns Result.Success(true)

            val result = dataSource.updateMovieState(movie)

            result shouldBeEqualTo Result.Success(true)
        }

    @Test
    fun `GIVEN movie update fails WHEN updateMovieState THEN returns error`() =
        runTest {
            val movie = mockk<Movie>()
            val exception = RuntimeException("Update error")

            coEvery { databaseRepository.updateMovieState(movie) } returns Result.Error(exception)

            val result = dataSource.updateMovieState(movie)

            result.shouldBeInstanceOf<Result.Error>()
        }
}
