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
internal class UploadWorker
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
                val movieId = inputData.getInt(MOVIE_ID_KEY, INVALID_MOVIE_ID)
                when {
                    movieId <= 0 -> Result.success()
                    accountDeletionRecoveryRepository.isLocalCleanupPending.first() ->
                        Result.success()
                    accountDeletionRecoveryRepository.isRemoteDeletionInFlight.first() ->
                        Result.success()
                    else -> {
                        val uid = currentUid()
                        if (uid == null) Result.success() else upload(uid, movieId)
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

        private suspend fun upload(
            uid: String,
            movieId: Int,
        ): Result =
            syncRepository
                .uploadMovie(uid, movieId)
                .rethrowCancellation()
                .fold(
                    onSuccess = {
                        observabilityTracker.trackAction(SYNC_UPLOAD_SUCCESS)
                        Result.success()
                    },
                    onFailure = ::retry,
                )

        private fun retry(exception: Throwable): Result {
            observabilityTracker.recordError(SYNC_UPLOAD_FAILED, exception)
            return Result.retry()
        }

        companion object {
            private const val INVALID_MOVIE_ID = -1
            private const val SYNC_UPLOAD_SUCCESS = "sync_upload_success"
            private const val SYNC_UPLOAD_FAILED = "sync_upload_failed"

            internal const val MOVIE_ID_KEY = "movie_id"
        }
    }
