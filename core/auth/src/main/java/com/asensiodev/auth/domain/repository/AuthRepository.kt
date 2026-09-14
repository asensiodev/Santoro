package com.asensiodev.auth.domain.repository

import com.asensiodev.auth.domain.model.ExpectedUserSignOutOutcome
import com.asensiodev.core.domain.model.SantoroUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<SantoroUser?>
    suspend fun signInAnonymously(): Result<SantoroUser>
    suspend fun signInWithGoogle(idToken: String): Result<SantoroUser>
    suspend fun linkWithGoogle(
        expectedUid: String,
        idToken: String,
    ): Result<SantoroUser>
    suspend fun reauthenticateWithGoogle(
        expectedUid: String,
        idToken: String,
    ): Result<Unit>
    suspend fun signOut(expectedUid: String): ExpectedUserSignOutOutcome
    suspend fun deleteAccount(expectedUid: String): Result<Unit>
}
