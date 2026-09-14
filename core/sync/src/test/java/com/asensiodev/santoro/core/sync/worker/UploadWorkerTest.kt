package com.asensiodev.santoro.core.sync.worker

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.repository.SyncRepository
import com.asensiodev.core.testing.relaxedMockk
import com.asensiodev.core.testing.verifyNever
import com.asensiodev.library.observability.api.ObservabilityTracker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UploadWorkerTest {
    private val authRepository: AuthRepository = mockk()
    private val deletionRecoveryRepository: AccountDeletionRecoveryRepository = mockk()
    private val syncRepository: SyncRepository = mockk()
    private val observabilityTracker: ObservabilityTracker = relaxedMockk()
    private val context: Context = mockk(relaxed = true)
    private val workerParams: WorkerParameters = mockk(relaxed = true)
    private lateinit var sut: UploadWorker

    @BeforeEach
    fun setUp() {
        every { workerParams.inputData } returns workDataOf(UploadWorker.MOVIE_ID_KEY to MOVIE_ID)
        every { deletionRecoveryRepository.isLocalCleanupPending } returns flowOf(false)
        every { deletionRecoveryRepository.isRemoteDeletionInFlight } returns flowOf(false)
        sut = newWorker()
    }

    @Test
    fun `GIVEN movie id WHEN work runs THEN uploads with current auth uid`() =
        runTest {
            every { authRepository.currentUser } returns flowOf(user())
            coEvery { syncRepository.uploadMovie(UID, MOVIE_ID) } returns kotlin.Result.success(Unit)

            sut.doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 1) { syncRepository.uploadMovie(UID, MOVIE_ID) }
        }

    @Test
    fun `GIVEN missing auth WHEN work runs THEN succeeds without upload`() =
        runTest {
            every { authRepository.currentUser } returns flowOf(null)

            sut.doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 0) { syncRepository.uploadMovie(any(), any()) }
        }

    @Test
    fun `GIVEN invalid movie id WHEN work runs THEN succeeds before gates`() =
        runTest {
            listOf(workDataOf(), workDataOf(UploadWorker.MOVIE_ID_KEY to 0)).forEach { input ->
                every { workerParams.inputData } returns input
                newWorker().doWork() shouldBeEqualTo Result.success()
            }

            coVerify(exactly = 0) { authRepository.currentUser }
            coVerify(exactly = 0) { syncRepository.uploadMovie(any(), any()) }
        }

    @Test
    fun `GIVEN deletion marker WHEN work runs THEN succeeds before reading auth`() =
        runTest {
            every { deletionRecoveryRepository.isLocalCleanupPending } returns flowOf(true)

            newWorker().doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 0) { authRepository.currentUser }
            coVerify(exactly = 0) { syncRepository.uploadMovie(any(), any()) }
        }

    @Test
    fun `GIVEN remote deletion lease WHEN work runs THEN succeeds before reading auth`() =
        runTest {
            every { deletionRecoveryRepository.isRemoteDeletionInFlight } returns flowOf(true)

            newWorker().doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 0) { authRepository.currentUser }
            coVerify(exactly = 0) { syncRepository.uploadMovie(any(), any()) }
        }

    @Test
    fun `GIVEN repository failure WHEN work runs THEN retries`() =
        runTest {
            every { authRepository.currentUser } returns flowOf(user())
            coEvery { syncRepository.uploadMovie(UID, MOVIE_ID) } returns
                kotlin.Result.failure(Exception("network"))

            sut.doWork() shouldBeEqualTo Result.retry()
        }

    @Test
    fun `GIVEN cancellation WHEN work runs THEN cancellation propagates without telemetry`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            every { authRepository.currentUser } returns flowOf(user())
            coEvery { syncRepository.uploadMovie(UID, MOVIE_ID) } returns kotlin.Result.failure(cancellation)

            runCatching { sut.doWork() }.exceptionOrNull() shouldBeEqualTo cancellation
            verifyNever { observabilityTracker.trackAction(any(), any()) }
            verifyNever { observabilityTracker.recordError(any(), any(), any()) }
        }

    private fun newWorker() =
        UploadWorker(
            context,
            workerParams,
            authRepository,
            deletionRecoveryRepository,
            syncRepository,
            observabilityTracker,
        )

    private fun user() = SantoroUser(UID, null, null, null, true)

    private companion object {
        const val UID = "uid123"
        const val MOVIE_ID = 42
    }
}
