package com.asensiodev.santoro.core.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
internal class SyncWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val authRepository: AuthRepository,
        private val syncRepository: SyncRepository,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val uid = authRepository.currentUser.firstOrNull()?.uid ?: return Result.success()
            val syncResult = syncRepository.downloadAndMerge(uid)
            return if (syncResult.isSuccess) Result.success() else Result.retry()
        }
    }
