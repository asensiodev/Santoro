package com.asensiodev.feature.moviedetail.impl.data.datasource

import app.cash.turbine.test
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
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

            coEvery { databaseRepository.getMovieById(movieId) } returns Result.success(movie)

            dataSource.getMovieDetail(movieId).test {
                awaitItem() shouldBeEqualTo Result.success(movie)
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN database error WHEN getMovieDetail THEN returns error`() =
        runTest {
            val movieId = 1
            val exception = RuntimeException("DB error")

            coEvery { databaseRepository.getMovieById(movieId) } returns Result.failure(exception)

            dataSource.getMovieDetail(movieId).test {
                val error = awaitItem()
                error.exceptionOrNull() shouldBeEqualTo exception
                awaitComplete()
            }
        }
}
