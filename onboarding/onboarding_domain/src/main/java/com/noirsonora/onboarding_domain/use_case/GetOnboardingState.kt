package com.noirsonora.onboarding_domain.use_case

import com.noirsonora.core.domain.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOnboardingState @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {
    operator fun invoke() : Flow<Boolean> {
        return dataStoreRepository.readOnboardingState()
    }
}