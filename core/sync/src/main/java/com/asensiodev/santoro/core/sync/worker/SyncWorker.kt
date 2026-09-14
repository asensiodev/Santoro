package com.asensiodev.santoro.core.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.repository.SyncRepository
import com.asensiodev.core.domain.result.rethrowCancellation
import com.asensiodev.library.observability.api.NoOpObservabilityTracker
import com.asensiodev.library.observability.api.ObservabilityTracker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
internal class SyncWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val authRepository: AuthRepository,
        private val accountDeletionRecoveryRepository: AccountDeletionRecoveryRepository,
        private val syncRepository: SyncRepository,
        private val observabilityTracker: ObservabilityTracker = NoOpObservabilityTracker,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result =
            try {
                when {
                    accountDeletionRecoveryRepository.isLocalCleanupPending.first() ->
                        Result.success()
                    accountDeletionRecoveryRepository.isRemoteDeletionInFlight.first() ->
                        Result.success()
                    else -> {
                        val uid = currentUid()
                        when {
                            uid == null -> Result.success()
                            inputData.getBoolean(UPLOAD_LOCAL_SNAPSHOT_KEY, false) ->
                                uploadAndDownload(uid)
                            else -> downloadAndMerge(uid)
                        }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                retry(exception)
            }

        private suspend fun currentUid(): String? =
            authRepository.currentUser
                .firstOrNull()
                ?.uid
                ?.takeIf(String::isNotBlank)

        private suspend fun uploadAndDownload(uid: String): Result =
            syncRepository
                .uploadLocalSnapshot(uid)
                .rethrowCancellation()
                .fold(
                    onSuccess = {
                        if (currentUid() == uid) downloadAndMerge(uid) else Result.success()
                    },
                    onFailure = ::retry,
                )

        private suspend fun downloadAndMerge(uid: String): Result =
            syncRepository
                .downloadAndMerge(uid)
                .rethrowCancellation()
                .fold(
                    onSuccess = {
                        observabilityTracker.trackAction(SYNC_DOWNLOAD_SUCCESS)
                        Result.success()
                    },
                    onFailure = ::retry,
                )

        private fun retry(exception: Throwable): Result {
            observabilityTracker.recordError(SYNC_DOWNLOAD_FAILED, exception)
            return Result.retry()
        }

        companion object {
            private const val SYNC_DOWNLOAD_SUCCESS = "sync_download_success"
            private const val SYNC_DOWNLOAD_FAILED = "sync_download_failed"
            internal const val UPLOAD_LOCAL_SNAPSHOT_KEY = "upload_local_snapshot"
        }
    }
