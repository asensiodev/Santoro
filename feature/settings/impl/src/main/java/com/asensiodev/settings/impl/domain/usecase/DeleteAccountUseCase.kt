package com.asensiodev.settings.impl.domain.usecase

import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.result.rethrowCancellation
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import javax.inject.Inject

internal class DeleteAccountUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val syncRepository: SyncRepository,
        private val databaseRepository: DatabaseRepository,
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

            val authDeletionResult =
                if (firestoreDeletionResult.isSuccess) {
                    authRepository
                        .deleteAccount(uid)
                        .rethrowCancellation()
                } else {
                    firestoreDeletionResult
                }

            return if (authDeletionResult.isSuccess) {
                databaseRepository
                    .clearAllUserData()
                    .rethrowCancellation()
            } else {
                authDeletionResult
            }
        }
    }
