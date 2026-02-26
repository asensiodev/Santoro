package com.asensiodev.core.domain.repository

import com.asensiodev.core.domain.model.ThemeOption
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val hasSeenGuestOnboarding: Flow<Boolean>
    val theme: Flow<ThemeOption>
    suspend fun setHasSeenGuestOnboarding(hasSeen: Boolean)
    suspend fun setTheme(option: ThemeOption)
}
