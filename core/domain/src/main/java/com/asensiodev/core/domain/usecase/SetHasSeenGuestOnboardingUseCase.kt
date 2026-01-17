package com.asensiodev.core.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetHasSeenGuestOnboardingUseCase
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        suspend operator fun invoke(hasSeen: Boolean) =
            withContext(dispatchers.io) {
                userPreferencesRepository.setHasSeenGuestOnboarding(hasSeen)
            }
    }
