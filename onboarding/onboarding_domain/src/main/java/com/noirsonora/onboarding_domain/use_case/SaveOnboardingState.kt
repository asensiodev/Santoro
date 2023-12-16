package com.noirsonora.onboarding_domain.use_case

import com.noirsonora.core.domain.DataStoreRepository
import javax.inject.Inject

class SaveOnboardingState @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {
    suspend operator fun invoke(completed: Boolean) {
        return dataStoreRepository.saveOnboardingState(completed)
    }
}