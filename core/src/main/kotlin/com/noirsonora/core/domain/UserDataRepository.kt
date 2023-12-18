package com.noirsonora.core.domain

import kotlinx.coroutines.flow.Flow


interface UserDataRepository {
    suspend fun saveOnboardingState(completed: Boolean)
    fun readOnboardingState(): Flow<Boolean>
}