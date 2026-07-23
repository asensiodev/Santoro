package com.asensiodev.santoro.core.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.library.observability.api.NoOpObservabilityTracker
import com.asensiodev.library.observability.api.ObservabilityTracker
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
internal class UploadWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val authRepository: AuthRepository,
        private val syncRepository: SyncRepository,
        private val observabilityTracker: ObservabilityTracker = NoOpObservabilityTracker,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val movieId = inputData.getInt(MOVIE_ID_KEY, INVALID_MOVIE_ID)
            val uid = authRepository.currentUser.firstOrNull()?.uid ?: return Result.success()
            val syncResult =
                if (movieId == INVALID_MOVIE_ID) {
                    syncRepository.uploadPendingChanges(uid)
                } else {
                    syncRepository.uploadMovie(uid, movieId)
                }
            return syncResult.fold(
                onSuccess = {
                    observabilityTracker.trackAction(SYNC_UPLOAD_SUCCESS)
                    Result.success()
                },
                onFailure = { exception ->
                    observabilityTracker.recordError(SYNC_UPLOAD_FAILED, exception)
                    Result.retry()
                },
            )
        }

        companion object {
            private const val INVALID_MOVIE_ID = -1
            private const val SYNC_UPLOAD_SUCCESS = "sync_upload_success"
            private const val SYNC_UPLOAD_FAILED = "sync_upload_failed"

            internal const val MOVIE_ID_KEY = "movie_id"
        }
    }
