package com.asensiodev.core.domain.usecase

import com.asensiodev.core.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetHasSeenGuestOnboardingUseCase
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
    ) {
        suspend operator fun invoke(hasSeen: Boolean) =
            userPreferencesRepository.setHasSeenGuestOnboarding(hasSeen)
    }
