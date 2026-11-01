package com.asensiodev.santoro.core.sync.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.repository.SyncRepository
import com.asensiodev.core.domain.repository.SyncScheduler

internal class FakeWorkerFactory(
    private val authRepository: AuthRepository,
    private val accountDeletionRecoveryRepository: AccountDeletionRecoveryRepository,
    private val syncRepository: SyncRepository,
    private val syncScheduler: SyncScheduler,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? =
        when (workerClassName) {
            UploadWorker::class.java.name -> {
                UploadWorker(
                    appContext,
                    workerParameters,
                    authRepository,
                    accountDeletionRecoveryRepository,
                    syncRepository,
                )
            }

            SyncWorker::class.java.name -> {
                SyncWorker(
                    appContext,
                    workerParameters,
                    authRepository,
                    accountDeletionRecoveryRepository,
                    syncRepository,
                    syncScheduler,
                )
            }

            else -> {
                null
            }
        }
}
