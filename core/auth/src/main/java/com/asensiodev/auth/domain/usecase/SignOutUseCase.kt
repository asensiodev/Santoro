package com.asensiodev.auth.domain.usecase

import com.asensiodev.auth.domain.model.ExpectedUserSignOutOutcome
import com.asensiodev.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(expectedUid: String): ExpectedUserSignOutOutcome =
            authRepository.signOut(expectedUid)
    }
