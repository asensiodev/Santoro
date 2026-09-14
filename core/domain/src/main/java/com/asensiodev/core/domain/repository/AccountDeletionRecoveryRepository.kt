package com.asensiodev.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface AccountDeletionRecoveryRepository {
    val isLocalCleanupPending: Flow<Boolean>
    val isRemoteDeletionInFlight: Flow<Boolean>

    suspend fun markLocalCleanupPending(): Result<Unit>

    suspend fun clearLocalCleanupPending(): Result<Unit>

    fun beginRemoteDeletion()

    fun completeRemoteDeletion()
}
