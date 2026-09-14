package com.asensiodev.core.domain.repository

interface SyncScheduler {
    fun schedulePeriodicSync()

    fun scheduleImmediateSync()

    fun enqueueUpload(movieId: Int)
}
