package com.asensiodev.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface AccountDeletionRecoveryRepository {
    val isLocalCleanupPending: Flow<Boolean>

    suspend fun markLocalCleanupPending(): Result<Unit>

    suspend fun clearLocalCleanupPending(): Result<Unit>
}
