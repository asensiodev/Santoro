package com.asensiodev.auth.domain.usecase

import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.SantoroUser
import javax.inject.Inject

class SignInAnonymouslyUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        suspend operator fun invoke(): Result<SantoroUser> = repository.signInAnonymously()
    }
