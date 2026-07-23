package com.asensiodev.santoro.core.sync.domain.repository

interface SyncRepository {
    suspend fun uploadMovie(
        uid: String,
        movieId: Int,
    ): Result<Unit>
    suspend fun uploadPendingChanges(uid: String): Result<Unit>
    suspend fun downloadAndMerge(uid: String): Result<Unit>
}
