package com.asensiodev.santoro.core.sync.worker

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.asensiodev.core.domain.Result as DomainResult

class SyncWorkerTest {
    private val authRepository: AuthRepository = mockk()
    private val syncRepository: SyncRepository = mockk()
    private val context: Context = mockk(relaxed = true)
    private val workerParams: WorkerParameters = mockk(relaxed = true)

    private lateinit var sut: SyncWorker

    @BeforeEach
    fun setUp() {
        sut = SyncWorker(context, workerParams, authRepository, syncRepository)
    }

    @Test
    fun `GIVEN no uid WHEN doWork THEN returns success no-op`() =
        runTest {
            coEvery { authRepository.currentUser } returns flowOf(null)

            val result = sut.doWork()

            result shouldBeEqualTo Result.success()
        }

    @Test
    fun `GIVEN uid and download succeeds WHEN doWork THEN returns success`() =
        runTest {
            val user = SantoroUser("uid123", null, null, null, true)
            coEvery { authRepository.currentUser } returns flowOf(user)
            coEvery { syncRepository.downloadAndMerge("uid123") } returns DomainResult.Success(Unit)

            val result = sut.doWork()

            result shouldBeEqualTo Result.success()
        }

    @Test
    fun `GIVEN uid and download fails WHEN doWork THEN returns retry`() =
        runTest {
            val user = SantoroUser("uid123", null, null, null, true)
            coEvery { authRepository.currentUser } returns flowOf(user)
            coEvery {
                syncRepository.downloadAndMerge("uid123")
            } returns DomainResult.Error(Exception("network"))

            val result = sut.doWork()

            result shouldBeEqualTo Result.retry()
        }
}
