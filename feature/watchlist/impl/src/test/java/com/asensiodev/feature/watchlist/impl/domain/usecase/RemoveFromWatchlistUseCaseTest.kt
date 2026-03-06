package com.asensiodev.feature.watchlist.impl.domain.usecase

import com.asensiodev.core.testing.coVerifyOnce
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveFromWatchlistUseCaseTest {
    private val repository: DatabaseRepository = mockk()
    private val dispatchers = TestDispatcherProvider()

    private lateinit var useCase: RemoveFromWatchlistUseCase

    @BeforeEach
    fun setUp() {
        useCase = RemoveFromWatchlistUseCase(repository, dispatchers)
    }

    @Test
    fun `GIVEN a movie id WHEN invoke THEN delegates to repository`() =
        runTest {
            val movieId = 1

            coEvery { repository.removeFromWatchlist(movieId) } returns Result.success(true)

            useCase(movieId)

            coVerifyOnce { repository.removeFromWatchlist(movieId) }
        }

    @Test
    fun `GIVEN success response WHEN invoke THEN returns success`() =
        runTest {
            val movieId = 1

            coEvery { repository.removeFromWatchlist(movieId) } returns Result.success(true)

            val result = useCase(movieId)

            result shouldBeEqualTo Result.success(true)
        }

    @Test
    fun `GIVEN repository throws exception WHEN invoke THEN returns error`() =
        runTest {
            val movieId = 1

            coEvery { repository.removeFromWatchlist(movieId) } returns Result.failure(RuntimeException("DB error"))

            val result = useCase(movieId)

            result.isFailure shouldBeEqualTo true
            result.exceptionOrNull().shouldBeInstanceOf<RuntimeException>()
        }
}
