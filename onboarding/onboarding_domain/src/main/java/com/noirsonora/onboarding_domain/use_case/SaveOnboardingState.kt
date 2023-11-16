package com.noirsonora.onboarding_domain.use_case

import com.noirsonora.onboarding_domain.repository.OnboardingRepository
import javax.inject.Inject

class SaveOnboardingState @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(completed: Boolean) {
        return onboardingRepository.saveOnboardingState(completed)
    }
}