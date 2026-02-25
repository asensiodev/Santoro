package com.asensiodev.santoro.core.sync.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository

internal class FakeWorkerFactory(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? =
        when (workerClassName) {
            UploadWorker::class.java.name -> {
                UploadWorker(appContext, workerParameters, authRepository, syncRepository)
            }

            SyncWorker::class.java.name -> {
                SyncWorker(appContext, workerParameters, authRepository, syncRepository)
            }

            else -> {
                null
            }
        }
}
