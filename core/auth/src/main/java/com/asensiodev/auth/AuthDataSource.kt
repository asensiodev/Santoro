package com.asensiodev.auth

import com.asensiodev.core.domain.model.SantoroUser
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    val currentUser: Flow<SantoroUser?>
    suspend fun signInAnonymously(): Result<SantoroUser>
    suspend fun signInWithCredential(credential: AuthCredential): Result<SantoroUser>
    suspend fun signOut()
}
