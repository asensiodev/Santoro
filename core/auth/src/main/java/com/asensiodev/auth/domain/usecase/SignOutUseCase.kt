package com.asensiodev.auth.domain.usecase

import com.asensiodev.auth.AuthDataSource
import javax.inject.Inject

class SignOutUseCase
    @Inject
    constructor(
        private val authDataSource: AuthDataSource,
    ) {
        suspend operator fun invoke() {
            authDataSource.signOut()
        }
    }
