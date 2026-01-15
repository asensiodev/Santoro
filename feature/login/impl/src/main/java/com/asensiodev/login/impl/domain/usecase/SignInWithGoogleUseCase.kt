package com.asensiodev.login.impl.domain.usecase

import com.asensiodev.auth.domain.repository.AuthRepository
import javax.inject.Inject

internal class SignInWithGoogleUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        suspend operator fun invoke(idToken: String) = repository.signInWithGoogle(idToken)
    }
