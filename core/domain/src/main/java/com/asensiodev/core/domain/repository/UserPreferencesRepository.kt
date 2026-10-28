package com.asensiodev.core.domain.repository

import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val hasSeenGuestOnboarding: Flow<Boolean>
    val hasSeenDetailTooltip: Flow<Boolean>
    val theme: Flow<ThemeOption>
    val watchedViewMode: Flow<WatchedViewMode>
    val watchedGrouping: Flow<WatchedGrouping>
    suspend fun setHasSeenGuestOnboarding(hasSeen: Boolean)
    suspend fun setHasSeenDetailTooltip(hasSeen: Boolean)
    suspend fun setTheme(option: ThemeOption)
    suspend fun setWatchedViewMode(mode: WatchedViewMode)
    suspend fun setWatchedGrouping(grouping: WatchedGrouping)
}
