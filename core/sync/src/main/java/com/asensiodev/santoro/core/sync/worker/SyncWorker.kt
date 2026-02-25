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
import com.asensiodev.core.domain.Result as DomainResult

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
            return when (syncRepository.downloadAndMerge(uid)) {
                is DomainResult.Success -> Result.success()
                is DomainResult.Error -> Result.retry()
            }
        }
    }
