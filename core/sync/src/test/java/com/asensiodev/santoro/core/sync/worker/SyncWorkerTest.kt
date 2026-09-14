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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SyncWorkerTest {
    private val authRepository: AuthRepository = mockk()
    private val deletionRecoveryRepository: AccountDeletionRecoveryRepository = mockk()
    private val syncRepository: SyncRepository = mockk()
    private val observabilityTracker: ObservabilityTracker = relaxedMockk()
    private val context: Context = mockk(relaxed = true)
    private val workerParams: WorkerParameters = mockk(relaxed = true)
    private lateinit var sut: SyncWorker

    @BeforeEach
    fun setUp() {
        every { workerParams.inputData } returns workDataOf(SyncWorker.UPLOAD_LOCAL_SNAPSHOT_KEY to true)
        every { deletionRecoveryRepository.isLocalCleanupPending } returns flowOf(false)
        every { deletionRecoveryRepository.isRemoteDeletionInFlight } returns flowOf(false)
        sut = newWorker()
    }

    @Test
    fun `GIVEN immediate mode WHEN work runs THEN reads current auth and uploads before download`() =
        runTest {
            every { authRepository.currentUser } returns flowOf(user())
            val calls = mutableListOf<String>()
            coEvery { syncRepository.uploadLocalSnapshot(UID) } coAnswers {
                calls += "upload"
                kotlin.Result.success(Unit)
            }
            coEvery { syncRepository.downloadAndMerge(UID) } coAnswers {
                calls += "download"
                kotlin.Result.success(Unit)
            }

            sut.doWork() shouldBeEqualTo Result.success()
            calls shouldBeEqualTo listOf("upload", "download")
        }

    @Test
    fun `GIVEN periodic mode WHEN work runs THEN downloads without upload`() =
        runTest {
            every { workerParams.inputData } returns workDataOf()
            every { authRepository.currentUser } returns flowOf(user())
            coEvery { syncRepository.downloadAndMerge(UID) } returns kotlin.Result.success(Unit)

            newWorker().doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 0) { syncRepository.uploadLocalSnapshot(any()) }
            coVerify(exactly = 1) { syncRepository.downloadAndMerge(UID) }
        }

    @Test
    fun `GIVEN no authenticated user WHEN work runs THEN succeeds without sync`() =
        runTest {
            every { authRepository.currentUser } returns flowOf(null)

            sut.doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 0) { syncRepository.uploadLocalSnapshot(any()) }
            coVerify(exactly = 0) { syncRepository.downloadAndMerge(any()) }
        }

    @Test
    fun `GIVEN deletion marker WHEN work runs THEN succeeds before reading auth`() =
        runTest {
            every { deletionRecoveryRepository.isLocalCleanupPending } returns flowOf(true)

            newWorker().doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 0) { authRepository.currentUser }
            coVerify(exactly = 0) { syncRepository.uploadLocalSnapshot(any()) }
        }

    @Test
    fun `GIVEN remote deletion lease WHEN work runs THEN succeeds before reading auth`() =
        runTest {
            every { deletionRecoveryRepository.isRemoteDeletionInFlight } returns flowOf(true)

            newWorker().doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 0) { authRepository.currentUser }
            coVerify(exactly = 0) { syncRepository.uploadLocalSnapshot(any()) }
        }

    @Test
    fun `GIVEN auth changes after upload WHEN immediate work continues THEN download does not start`() =
        runTest {
            val currentUser = MutableStateFlow<SantoroUser?>(user())
            every { authRepository.currentUser } returns currentUser
            coEvery { syncRepository.uploadLocalSnapshot(UID) } coAnswers {
                currentUser.value = user("other-uid")
                kotlin.Result.success(Unit)
            }

            sut.doWork() shouldBeEqualTo Result.success()

            coVerify(exactly = 0) { syncRepository.downloadAndMerge(any()) }
        }

    @Test
    fun `GIVEN repository failure WHEN work runs THEN retries`() =
        runTest {
            every { authRepository.currentUser } returns flowOf(user())
            coEvery { syncRepository.uploadLocalSnapshot(UID) } returns
                kotlin.Result.failure(Exception("network"))

            sut.doWork() shouldBeEqualTo Result.retry()
        }

    @Test
    fun `GIVEN cancellation WHEN work runs THEN cancellation propagates without telemetry`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            every { authRepository.currentUser } returns flowOf(user())
            coEvery { syncRepository.uploadLocalSnapshot(UID) } returns kotlin.Result.failure(cancellation)

            runCatching { sut.doWork() }.exceptionOrNull() shouldBeEqualTo cancellation
            verifyNever { observabilityTracker.trackAction(any(), any()) }
            verifyNever { observabilityTracker.recordError(any(), any(), any()) }
        }

    private fun newWorker() =
        SyncWorker(
            context,
            workerParams,
            authRepository,
            deletionRecoveryRepository,
            syncRepository,
            observabilityTracker,
        )

    private fun user(uid: String = UID) = SantoroUser(uid, null, null, null, true)

    private companion object {
        const val UID = "uid123"
    }
}
