package com.asensiodev.core.domain.repository

interface SyncRepository {
    suspend fun uploadMovie(
        uid: String,
        movieId: Int,
    ): Result<Unit>

    suspend fun uploadLocalSnapshot(uid: String): Result<Unit>

    suspend fun downloadAndMerge(uid: String): Result<Unit>

    suspend fun deleteUserData(uid: String): Result<Unit>
}
