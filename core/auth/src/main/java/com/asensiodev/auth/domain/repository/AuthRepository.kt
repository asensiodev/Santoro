package com.asensiodev.auth.domain.repository

import com.asensiodev.core.domain.model.SantoroUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<SantoroUser?>
    suspend fun signOut()
}
