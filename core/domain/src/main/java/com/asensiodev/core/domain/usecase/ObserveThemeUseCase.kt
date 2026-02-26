package com.asensiodev.core.domain.usecase

import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveThemeUseCase
    @Inject
    constructor(
        private val repository: UserPreferencesRepository,
    ) {
        operator fun invoke(): Flow<ThemeOption> = repository.theme
    }
