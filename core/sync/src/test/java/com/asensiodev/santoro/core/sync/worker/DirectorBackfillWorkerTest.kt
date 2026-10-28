package com.asensiodev.santoro.core.sync.worker

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.asensiodev.core.domain.model.Director
import com.asensiodev.core.domain.repository.MovieMutationRepository
import com.asensiodev.core.domain.repository.SyncScheduler
import com.asensiodev.core.domain.repository.SyncStore
import com.asensiodev.core.testing.relaxedMockk
import com.asensiodev.library.observability.api.ObservabilityTracker
import com.asensiodev.santoro.core.sync.data.datasource.MovieDirectorsRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

class DirectorBackfillWorkerTest {
    private val syncStore: SyncStore = mockk()
    private val movieMutationRepository: MovieMutationRepository = mockk()
    private val directorsDataSource: MovieDirectorsRemoteDataSource = mockk()
    private val syncScheduler: SyncScheduler = relaxedMockk()
    private val observabilityTracker: ObservabilityTracker = relaxedMockk()
    private val sut =
        DirectorBackfillWorker(
            mockk<Context>(relaxed = true),
            mockk<WorkerParameters>(relaxed = true),
            syncStore,
            movieMutationRepository,
            directorsDataSource,
            syncScheduler,
            observabilityTracker,
        )

    @Test
    fun `GIVEN unresolved watched movies WHEN work runs THEN stores directors and syncs once`() =
        runTest {
            coEvery { syncStore.getWatchedMovieIdsWithUnresolvedDirectors() } returns
                kotlin.Result.success(listOf(115, 404))
            coEvery { directorsDataSource.getDirectors(115) } returns kotlin.Result.success(COEN_BROTHERS)
            coEvery { directorsDataSource.getDirectors(404) } returns kotlin.Result.success(emptyList())
            coEvery { movieMutationRepository.storeResolvedDirectors(any(), any()) } returns kotlin.Result.success(true)

            sut.doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 1) { movieMutationRepository.storeResolvedDirectors(115, COEN_BROTHERS) }
            coVerify(exactly = 1) { movieMutationRepository.storeResolvedDirectors(404, emptyList()) }
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }
            verify {
                observabilityTracker.trackAction(
                    "director_backfill_finished",
                    mapOf("resolved" to "2", "remaining" to "0"),
                )
            }
        }

    @Test
    fun `GIVEN nothing unresolved WHEN work runs THEN succeeds without requests or sync`() =
        runTest {
            coEvery { syncStore.getWatchedMovieIdsWithUnresolvedDirectors() } returns kotlin.Result.success(emptyList())

            sut.doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 0) { directorsDataSource.getDirectors(any()) }
            verify(exactly = 0) { syncScheduler.scheduleImmediateSync() }
            verify {
                observabilityTracker.trackAction(
                    "director_backfill_finished",
                    mapOf("resolved" to "0", "remaining" to "0"),
                )
            }
        }

    @Test
    fun `GIVEN transient failure mid run WHEN work runs THEN keeps resolved movies and retries`() =
        runTest {
            coEvery { syncStore.getWatchedMovieIdsWithUnresolvedDirectors() } returns
                kotlin.Result.success(listOf(115, 116, 117))
            coEvery { directorsDataSource.getDirectors(115) } returns kotlin.Result.success(COEN_BROTHERS)
            coEvery { directorsDataSource.getDirectors(116) } returns kotlin.Result.failure(IOException("Offline"))
            coEvery { movieMutationRepository.storeResolvedDirectors(any(), any()) } returns kotlin.Result.success(true)

            sut.doWork() shouldBeEqualTo Result.retry()

            coVerify(exactly = 1) { movieMutationRepository.storeResolvedDirectors(115, COEN_BROTHERS) }
            coVerify(exactly = 0) { directorsDataSource.getDirectors(117) }
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }
            verify {
                observabilityTracker.trackAction(
                    "director_backfill_finished",
                    mapOf("resolved" to "1", "remaining" to "2"),
                )
            }
        }

    @Test
    fun `GIVEN local read failure WHEN work runs THEN retries without requests`() =
        runTest {
            coEvery { syncStore.getWatchedMovieIdsWithUnresolvedDirectors() } returns
                kotlin.Result.failure(IllegalStateException("Database closed"))

            sut.doWork() shouldBeEqualTo Result.retry()

            coVerify(exactly = 0) { directorsDataSource.getDirectors(any()) }
        }

    @Test
    fun `GIVEN work is cancelled WHEN a request is running THEN cancellation propagates without failure`() =
        runTest {
            coEvery { syncStore.getWatchedMovieIdsWithUnresolvedDirectors() } returns kotlin.Result.success(listOf(115))
            coEvery { directorsDataSource.getDirectors(115) } throws CancellationException("Stopped")

            assertThrows<CancellationException> { sut.doWork() }

            verify(exactly = 0) { observabilityTracker.recordError(any(), any(), any()) }
            verify(exactly = 0) { syncScheduler.scheduleImmediateSync() }
        }

    private companion object {
        val COEN_BROTHERS = listOf(Director(1223, "Joel Coen"), Director(1224, "Ethan Coen"))
    }
}
