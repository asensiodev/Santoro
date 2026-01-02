package com.asensiodev.impl.domain.usecase

import com.asensiodev.auth.domain.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject

internal class SignInWithCredentialUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        suspend operator fun invoke(credential: AuthCredential) =
            repository.signInWithCredential(credential)
    }
