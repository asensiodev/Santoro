package com.asensiodev.santoro.core.sync.domain.repository

import com.asensiodev.core.domain.Result

interface SyncRepository {
    suspend fun uploadPendingChanges(uid: String): Result<Unit>
    suspend fun downloadAndMerge(uid: String): Result<Unit>
}
