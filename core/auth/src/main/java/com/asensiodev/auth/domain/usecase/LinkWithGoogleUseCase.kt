package com.asensiodev.auth.domain.usecase

import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.SantoroUser
import javax.inject.Inject

class LinkWithGoogleUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(idToken: String): Result<SantoroUser> =
            authRepository.linkWithGoogle(idToken)
    }
