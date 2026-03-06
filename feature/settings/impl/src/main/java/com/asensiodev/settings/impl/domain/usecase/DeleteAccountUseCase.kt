package com.asensiodev.settings.impl.domain.usecase

import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import javax.inject.Inject

internal class DeleteAccountUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
    ) {
        suspend operator fun invoke(): Result<Unit> {
            databaseRepository.clearAllUserData()
            return authRepository.deleteAccount()
        }
    }
