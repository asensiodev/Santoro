package com.noirsonora.onboarding_domain.use_case

import com.noirsonora.onboarding_domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOnboardingState @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) {
    operator fun invoke() : Flow<Boolean> {
        return onboardingRepository.getOnboardingState()
    }
}