package com.asensiodev.settings.impl.domain.usecase

import com.asensiodev.auth.domain.exception.AuthenticatedUserMismatchException
import com.asensiodev.auth.domain.exception.NoAuthenticatedUserException
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.repository.SyncRepository
import com.asensiodev.core.domain.result.rethrowCancellation
import kotlinx.coroutines.flow.first
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
            if (reauthenticationResult.isFailure) return reauthenticationResult

            recoveryRepository.beginRemoteDeletion()
            try {
                return deleteAuthenticatedUserData(uid)
            } finally {
                recoveryRepository.completeRemoteDeletion()
            }
        }

        private suspend fun deleteAuthenticatedUserData(uid: String): Result<Unit> {
            val currentUser = authRepository.currentUser.first()
            val currentUserFailure =
                when {
                    currentUser == null -> NoAuthenticatedUserException()
                    currentUser.uid != uid -> AuthenticatedUserMismatchException()
                    else -> null
                }
            if (currentUserFailure != null) return Result.failure(currentUserFailure)

            val firestoreDeletionResult =
                syncRepository
                    .deleteUserData(uid)
                    .rethrowCancellation()

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

            val finalResult =
                if (markerResult.isSuccess && authDeletionResult.isFailure) {
                    val markerClearResult =
                        recoveryRepository
                            .clearLocalCleanupPending()
                            .rethrowCancellation()
                    if (markerClearResult.isFailure) markerClearResult else authDeletionResult
                } else {
                    authDeletionResult
                }

            return finalResult
        }
    }
