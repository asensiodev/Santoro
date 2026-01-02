package com.asensiodev.auth.data.repository

import com.asensiodev.auth.AuthDataSource
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.SantoroUser
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class DefaultAuthRepository
    @Inject
    constructor(
        private val dataSource: AuthDataSource,
    ) : AuthRepository {
        override val currentUser: Flow<SantoroUser?> = dataSource.currentUser

        override suspend fun signInAnonymously(): Result<SantoroUser> =
            dataSource.signInAnonymously()

        override suspend fun signInWithCredential(credential: AuthCredential): Result<SantoroUser> =
            dataSource.signInWithCredential(credential)

        override suspend fun signOut() = dataSource.signOut()
    }
