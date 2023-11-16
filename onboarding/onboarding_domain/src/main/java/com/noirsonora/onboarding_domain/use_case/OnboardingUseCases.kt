package com.noirsonora.onboarding_domain.use_case

data class OnboardingUseCases(
    val saveOnboardingState: SaveOnboardingState,
    val getOnboardingState: GetOnboardingState
)
