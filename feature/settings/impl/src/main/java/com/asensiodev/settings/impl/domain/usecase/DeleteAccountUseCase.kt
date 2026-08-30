package com.asensiodev.settings.impl.domain.usecase

import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.result.rethrowCancellation
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import javax.inject.Inject

internal class DeleteAccountUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val syncRepository: SyncRepository,
        private val recoveryRepository: AccountDeletionRecoveryRepository,
    ) {
        suspend operator fun invoke(
            uid: String,
            idToken: String,
        ): Result<Unit> {
            val reauthenticationResult =
                authRepository
                    .reauthenticateWithGoogle(uid, idToken)
                    .rethrowCancellation()

            val firestoreDeletionResult =
                if (reauthenticationResult.isSuccess) {
                    syncRepository
                        .deleteUserData(uid)
                        .rethrowCancellation()
                } else {
                    reauthenticationResult
                }

            val markerResult =
                if (firestoreDeletionResult.isSuccess) {
                    recoveryRepository
                        .markLocalCleanupPending()
                        .rethrowCancellation()
                } else {
                    firestoreDeletionResult
                }

            val authDeletionResult =
                if (markerResult.isSuccess) {
                    authRepository
                        .deleteAccount(uid)
                        .rethrowCancellation()
                } else {
                    markerResult
                }

            if (markerResult.isSuccess && authDeletionResult.isFailure) {
                val markerClearResult =
                    recoveryRepository
                        .clearLocalCleanupPending()
                        .rethrowCancellation()
                if (markerClearResult.isFailure) return markerClearResult
            }

            return authDeletionResult
        }
    }
