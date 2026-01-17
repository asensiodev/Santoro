package com.asensiodev.core.domain.usecase

import com.asensiodev.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHasSeenGuestOnboardingUseCase
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
    ) {
        operator fun invoke(): Flow<Boolean> = userPreferencesRepository.hasSeenGuestOnboarding
    }
