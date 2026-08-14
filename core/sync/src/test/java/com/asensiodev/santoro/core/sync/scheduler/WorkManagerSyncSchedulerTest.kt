package com.asensiodev.santoro.core.sync.scheduler

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.impl.WorkManagerImpl
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.asensiodev.santoro.core.sync.worker.SyncWorker
import com.asensiodev.santoro.core.sync.worker.UploadWorker
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class WorkManagerSyncSchedulerTest {
    private lateinit var context: Context
    private lateinit var workerExecutor: ExecutorService
    private lateinit var workManager: WorkManager
    private lateinit var sut: WorkManagerSyncScheduler

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        workerExecutor = Executors.newSingleThreadExecutor()
        val configuration =
            Configuration
                .Builder()
                .setExecutor(workerExecutor)
                .setTaskExecutor(SynchronousExecutor())
                .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
        workManager = WorkManager.getInstance(context)
        sut = WorkManagerSyncScheduler(context)
    }

    @After
    fun tearDown() {
        WorkManagerTestInitHelper.closeWorkDatabase()
        workerExecutor.shutdownNow()
    }

    @Test
    fun `GIVEN periodic sync scheduled twice WHEN inspected THEN connected six-hour work is retained`() {
        sut.schedulePeriodicSync()

        val first = uniqueWork(PERIODIC_SYNC_WORK_NAME).single()

        sut.schedulePeriodicSync()

        val retained = uniqueWork(PERIODIC_SYNC_WORK_NAME).single()
        retained.id shouldBeEqualTo first.id
        retained.state shouldBeEqualTo WorkInfo.State.ENQUEUED
        workSpecWorkerClassName(retained) shouldBeEqualTo SyncWorker::class.java.name
        retained.constraints.requiredNetworkType shouldBeEqualTo NetworkType.CONNECTED
        retained.periodicityInfo?.repeatIntervalMillis shouldBeEqualTo TimeUnit.HOURS.toMillis(6)
        retained.periodicityInfo?.flexIntervalMillis shouldBeEqualTo TimeUnit.HOURS.toMillis(6)
    }

    @Test
    fun `GIVEN immediate sync scheduled twice WHEN inspected THEN connected work is replaced`() {
        sut.scheduleImmediateSync()

        val first = uniqueWork(IMMEDIATE_SYNC_WORK_NAME).single()

        sut.scheduleImmediateSync()

        val replacement = uniqueWork(IMMEDIATE_SYNC_WORK_NAME).single()
        (replacement.id != first.id) shouldBeEqualTo true
        replacement.state shouldBeEqualTo WorkInfo.State.ENQUEUED
        workSpecWorkerClassName(replacement) shouldBeEqualTo SyncWorker::class.java.name
        replacement.constraints.requiredNetworkType shouldBeEqualTo NetworkType.CONNECTED
    }

    @Test
    fun `GIVEN uploads scheduled WHEN inspected THEN names tags input constraints and replacement are preserved`() {
        sut.enqueueUpload(FIRST_MOVIE_ID)

        val first = uniqueWork(uploadWorkName(FIRST_MOVIE_ID)).single()

        sut.enqueueUpload(FIRST_MOVIE_ID)
        sut.enqueueUpload(SECOND_MOVIE_ID)

        val replacement = uniqueWork(uploadWorkName(FIRST_MOVIE_ID)).single()
        val independent = uniqueWork(uploadWorkName(SECOND_MOVIE_ID)).single()
        (replacement.id != first.id) shouldBeEqualTo true
        replacement.state shouldBeEqualTo WorkInfo.State.ENQUEUED
        workSpecWorkerClassName(replacement) shouldBeEqualTo UploadWorker::class.java.name
        workSpecWorkerClassName(independent) shouldBeEqualTo UploadWorker::class.java.name
        replacement.constraints.requiredNetworkType shouldBeEqualTo NetworkType.CONNECTED
        replacement.tags.contains(uploadWorkName(FIRST_MOVIE_ID)) shouldBeEqualTo true
        independent.tags.contains(uploadWorkName(SECOND_MOVIE_ID)) shouldBeEqualTo true
        workSpecInput(replacement)?.getInt(UploadWorker.MOVIE_ID_KEY, -1) shouldBeEqualTo FIRST_MOVIE_ID
        workSpecInput(independent)?.getInt(UploadWorker.MOVIE_ID_KEY, -1) shouldBeEqualTo SECOND_MOVIE_ID
    }

    private fun uniqueWork(name: String): List<WorkInfo> = workManager.getWorkInfosForUniqueWork(name).get()

    private fun workSpecInput(workInfo: WorkInfo) =
        (workManager as WorkManagerImpl)
            .workDatabase
            .workSpecDao()
            .getWorkSpec(workInfo.id.toString())
            ?.input

    private fun workSpecWorkerClassName(workInfo: WorkInfo) =
        (workManager as WorkManagerImpl)
            .workDatabase
            .workSpecDao()
            .getWorkSpec(workInfo.id.toString())
            ?.workerClassName

    private fun uploadWorkName(movieId: Int) = "$UPLOAD_WORK_NAME_PREFIX$movieId"

    private companion object {
        const val PERIODIC_SYNC_WORK_NAME = "santoro_periodic_sync"
        const val IMMEDIATE_SYNC_WORK_NAME = "santoro_immediate_sync"
        const val UPLOAD_WORK_NAME_PREFIX = "santoro_upload_movie_"
        const val FIRST_MOVIE_ID = 42
        const val SECOND_MOVIE_ID = 84
    }
}
