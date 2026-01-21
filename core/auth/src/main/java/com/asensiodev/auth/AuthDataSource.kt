package com.asensiodev.auth

import com.asensiodev.core.domain.model.SantoroUser
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    val currentUser: Flow<SantoroUser?>
    suspend fun signInAnonymously(): Result<SantoroUser>
    suspend fun signInWithGoogle(idToken: String): Result<SantoroUser>
    suspend fun linkWithGoogle(idToken: String): Result<SantoroUser>
    suspend fun signOut()
}
