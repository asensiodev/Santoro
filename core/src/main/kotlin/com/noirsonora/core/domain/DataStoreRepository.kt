package com.noirsonora.core.domain

import kotlinx.coroutines.flow.Flow


interface DataStoreRepository {
    suspend fun saveOnboardingState(completed: Boolean)
    fun readOnboardingState(): Flow<Boolean>
}