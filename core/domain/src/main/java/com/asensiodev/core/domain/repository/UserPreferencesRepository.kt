package com.asensiodev.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val hasSeenGuestOnboarding: Flow<Boolean>
    suspend fun setHasSeenGuestOnboarding(hasSeen: Boolean)
}
