package com.noirsonora.onboarding_domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    suspend fun saveOnboardingState(completed: Boolean)
    fun getOnboardingState(): Flow<Boolean>
}