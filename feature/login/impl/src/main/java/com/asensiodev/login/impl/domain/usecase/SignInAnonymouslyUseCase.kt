package com.asensiodev.login.impl.domain.usecase

import com.asensiodev.auth.domain.repository.AuthRepository
import javax.inject.Inject

internal class SignInAnonymouslyUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        suspend operator fun invoke() = repository.signInAnonymously()
    }
