package com.asensiodev.santoro.core.sync.scheduler

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.asensiodev.santoro.core.sync.worker.SyncWorker
import com.asensiodev.santoro.core.sync.worker.UploadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val PERIODIC_SYNC_WORK_NAME = "santoro_periodic_sync"
private const val UPLOAD_WORK_NAME_PREFIX = "santoro_upload_movie_"
private const val IMMEDIATE_SYNC_WORK_NAME = "santoro_immediate_sync"
private const val SYNC_INTERVAL_HOURS = 6L

@Singleton
class WorkManagerSyncScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val networkConstraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        fun schedulePeriodicSync() {
            val request =
                PeriodicWorkRequestBuilder<SyncWorker>(
                    SYNC_INTERVAL_HOURS,
                    TimeUnit.HOURS,
                ).setConstraints(networkConstraints)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun scheduleImmediateSync() {
            val request =
                OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(networkConstraints)
                    .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        fun enqueueUpload(movieId: Int) {
            val request =
                OneTimeWorkRequestBuilder<UploadWorker>()
                    .setConstraints(networkConstraints)
                    .addTag("$UPLOAD_WORK_NAME_PREFIX$movieId")
                    .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "$UPLOAD_WORK_NAME_PREFIX$movieId",
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
