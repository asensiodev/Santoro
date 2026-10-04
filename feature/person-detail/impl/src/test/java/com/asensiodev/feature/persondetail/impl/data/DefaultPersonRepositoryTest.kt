package com.asensiodev.feature.persondetail.impl.data

import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

class DefaultPersonRepositoryTest {
    private val service = mockk<PersonApiService>()
    private val repository = DefaultPersonRepository(service, TestDispatcherProvider())

    @Test
    fun `GIVEN successful endpoints WHEN requested THEN mapped person and movie credits are returned`() =
        runTest {
            val profile = PersonApiModel(7, "Person", "Biography", null, null, null, null)
            coEvery { service.person(7) } returns profile
            coEvery { service.movieCredits(7) } returns PersonCreditsApiModel(null, null)
            repository.getPerson(7).getOrThrow() shouldBeEqualTo profile.toDomain()
            repository.getMovieCredits(7).getOrThrow() shouldBeEqualTo PersonCreditsApiModel(null, null).toDomain()
        }

    @Test
    fun `GIVEN network failure WHEN requested THEN the error is returned`() =
        runTest {
            coEvery { service.person(7) } throws IOException("Offline")
            coEvery { service.movieCredits(7) } throws IOException("Offline")
            repository.getPerson(7).exceptionOrNull().shouldBeInstanceOf<IOException>()
            repository.getMovieCredits(7).exceptionOrNull().shouldBeInstanceOf<IOException>()
        }

    @Test
    fun `GIVEN another person response WHEN requested THEN mismatched identity is rejected`() =
        runTest {
            coEvery { service.person(7) } returns PersonApiModel(8, "Other", null, null, null, null, null)
            repository.getPerson(7).exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }

    @Test
    fun `GIVEN invalid profile WHEN requested THEN it becomes a retryable failure`() =
        runTest {
            coEvery { service.person(7) } returns PersonApiModel(7, null, null, null, null, null, null)
            repository.getPerson(7).exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }

    @Test
    fun `GIVEN cancelled request WHEN either endpoint loads THEN cancellation is preserved`() =
        runTest {
            coEvery { service.person(7) } throws CancellationException("Leave")
            coEvery { service.movieCredits(7) } throws CancellationException("Leave")
            assertThrows<CancellationException> { repository.getPerson(7) }
            assertThrows<CancellationException> { repository.getMovieCredits(7) }
        }
}
