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
import com.asensiodev.santoro.core.sync.data.datasource.MovieMetadataRemoteDataSource
import com.asensiodev.santoro.core.sync.data.datasource.RemoteMovieMetadata
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
    private val metadataDataSource: MovieMetadataRemoteDataSource = mockk()
    private val syncScheduler: SyncScheduler = relaxedMockk()
    private val observabilityTracker: ObservabilityTracker = relaxedMockk()
    private val sut =
        DirectorBackfillWorker(
            mockk<Context>(relaxed = true),
            mockk<WorkerParameters>(relaxed = true),
            syncStore,
            movieMutationRepository,
            metadataDataSource,
            syncScheduler,
            observabilityTracker,
        )

    @Test
    fun `GIVEN watched movie missing only its release date WHEN work runs THEN stores the date and syncs`() =
        runTest {
            coEvery { syncStore.getWatchedMovieIdsWithUnresolvedMetadata() } returns kotlin.Result.success(listOf(275))
            coEvery { metadataDataSource.getMetadata(275) } returns
                kotlin.Result.success(RemoteMovieMetadata(COEN_BROTHERS, FARGO_RELEASE_DATE))
            coEvery { movieMutationRepository.storeResolvedMetadata(any(), any(), any()) } returns
                kotlin.Result.success(true)

            sut.doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 1) {
                movieMutationRepository.storeResolvedMetadata(275, COEN_BROTHERS, FARGO_RELEASE_DATE)
            }
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `GIVEN unresolved watched movies WHEN work runs THEN stores directors and syncs once`() =
        runTest {
            coEvery { syncStore.getWatchedMovieIdsWithUnresolvedMetadata() } returns
                kotlin.Result.success(listOf(115, 404))
            coEvery { metadataDataSource.getMetadata(115) } returns
                kotlin.Result.success(RemoteMovieMetadata(COEN_BROTHERS, ""))
            coEvery { metadataDataSource.getMetadata(404) } returns
                kotlin.Result.success(RemoteMovieMetadata(emptyList(), ""))
            coEvery { movieMutationRepository.storeResolvedMetadata(any(), any(), any()) } returns
                kotlin.Result.success(true)

            sut.doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 1) { movieMutationRepository.storeResolvedMetadata(115, COEN_BROTHERS, any()) }
            coVerify(exactly = 1) { movieMutationRepository.storeResolvedMetadata(404, emptyList(), any()) }
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
            coEvery { syncStore.getWatchedMovieIdsWithUnresolvedMetadata() } returns kotlin.Result.success(emptyList())

            sut.doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 0) { metadataDataSource.getMetadata(any()) }
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
            coEvery { syncStore.getWatchedMovieIdsWithUnresolvedMetadata() } returns
                kotlin.Result.success(listOf(115, 116, 117))
            coEvery { metadataDataSource.getMetadata(115) } returns
                kotlin.Result.success(RemoteMovieMetadata(COEN_BROTHERS, ""))
            coEvery { metadataDataSource.getMetadata(116) } returns kotlin.Result.failure(IOException("Offline"))
            coEvery { movieMutationRepository.storeResolvedMetadata(any(), any(), any()) } returns
                kotlin.Result.success(true)

            sut.doWork() shouldBeEqualTo Result.retry()

            coVerify(exactly = 1) { movieMutationRepository.storeResolvedMetadata(115, COEN_BROTHERS, any()) }
            coVerify(exactly = 0) { metadataDataSource.getMetadata(117) }
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
            coEvery { syncStore.getWatchedMovieIdsWithUnresolvedMetadata() } returns
                kotlin.Result.failure(IllegalStateException("Database closed"))

            sut.doWork() shouldBeEqualTo Result.retry()

            coVerify(exactly = 0) { metadataDataSource.getMetadata(any()) }
        }

    @Test
    fun `GIVEN work is cancelled WHEN a request is running THEN cancellation propagates without failure`() =
        runTest {
            coEvery { syncStore.getWatchedMovieIdsWithUnresolvedMetadata() } returns kotlin.Result.success(listOf(115))
            coEvery { metadataDataSource.getMetadata(115) } throws CancellationException("Stopped")

            assertThrows<CancellationException> { sut.doWork() }

            verify(exactly = 0) { observabilityTracker.recordError(any(), any(), any()) }
            verify(exactly = 0) { syncScheduler.scheduleImmediateSync() }
        }

    private companion object {
        const val FARGO_RELEASE_DATE = "1996-03-08"
        val COEN_BROTHERS = listOf(Director(1223, "Joel Coen"), Director(1224, "Ethan Coen"))
    }
}
