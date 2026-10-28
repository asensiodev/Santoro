package com.asensiodev.santoro.core.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asensiodev.core.domain.repository.MovieMutationRepository
import com.asensiodev.core.domain.repository.SyncScheduler
import com.asensiodev.core.domain.repository.SyncStore
import com.asensiodev.core.domain.result.rethrowCancellation
import com.asensiodev.library.observability.api.ObservabilityTracker
import com.asensiodev.santoro.core.sync.data.datasource.MovieDirectorsRemoteDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
internal class DirectorBackfillWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val syncStore: SyncStore,
        private val movieMutationRepository: MovieMutationRepository,
        private val directorsDataSource: MovieDirectorsRemoteDataSource,
        private val syncScheduler: SyncScheduler,
        private val observabilityTracker: ObservabilityTracker,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val movieIds =
                syncStore
                    .getWatchedMovieIdsWithUnresolvedDirectors()
                    .rethrowCancellation()
                    .getOrElse { exception -> return retry(exception) }
            var resolvedCount = 0
            var failure: Throwable? = null
            for (movieId in movieIds) {
                failure = resolve(movieId).exceptionOrNull()
                if (failure != null) break
                resolvedCount++
            }
            if (resolvedCount > 0) syncScheduler.scheduleImmediateSync()
            observabilityTracker.trackAction(
                DIRECTOR_BACKFILL_FINISHED,
                mapOf(
                    RESOLVED_PARAMETER to resolvedCount.toString(),
                    REMAINING_PARAMETER to (movieIds.size - resolvedCount).toString(),
                ),
            )
            return failure?.let(::retry) ?: Result.success()
        }

        private suspend fun resolve(movieId: Int): kotlin.Result<Unit> {
            val directors =
                directorsDataSource
                    .getDirectors(movieId)
                    .rethrowCancellation()
                    .getOrElse { exception -> return kotlin.Result.failure(exception) }
            return movieMutationRepository
                .storeResolvedDirectors(movieId, directors)
                .rethrowCancellation()
                .map { }
        }

        private fun retry(exception: Throwable): Result {
            observabilityTracker.recordError(DIRECTOR_BACKFILL_FAILED, exception)
            return Result.retry()
        }

        private companion object {
            const val DIRECTOR_BACKFILL_FINISHED = "director_backfill_finished"
            const val DIRECTOR_BACKFILL_FAILED = "director_backfill_failed"
            const val RESOLVED_PARAMETER = "resolved"
            const val REMAINING_PARAMETER = "remaining"
        }
    }
