package com.asensiodev.santoro.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SharedPreferencesAccountDeletionRecoveryRepository internal constructor(
    private val preferences: SharedPreferences,
) : AccountDeletionRecoveryRepository {
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : this(
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
    )

    private val _isLocalCleanupPending =
        MutableStateFlow(preferences.getBoolean(KEY_LOCAL_CLEANUP_PENDING, false))
    override val isLocalCleanupPending: Flow<Boolean> = _isLocalCleanupPending.asStateFlow()
    private val _isRemoteDeletionInFlight = MutableStateFlow(false)
    override val isRemoteDeletionInFlight: Flow<Boolean> =
        _isRemoteDeletionInFlight.asStateFlow()
    override suspend fun markLocalCleanupPending(): Result<Unit> = updatePendingState(true)

    override suspend fun clearLocalCleanupPending(): Result<Unit> = updatePendingState(false)

    override fun beginRemoteDeletion() {
        _isRemoteDeletionInFlight.value = true
    }

    override fun completeRemoteDeletion() {
        _isRemoteDeletionInFlight.value = false
    }

    private fun updatePendingState(isPending: Boolean): Result<Unit> =
        runCatching {
            check(
                preferences
                    .edit()
                    .putBoolean(KEY_LOCAL_CLEANUP_PENDING, isPending)
                    .commit(),
            )
            _isLocalCleanupPending.value = isPending
        }

    private companion object {
        const val PREFERENCES_NAME = "account_deletion_recovery"
        const val KEY_LOCAL_CLEANUP_PENDING = "local_cleanup_pending"
    }
}
