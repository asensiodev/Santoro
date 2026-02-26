package com.asensiodev.core.domain.usecase

import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetThemeUseCase
    @Inject
    constructor(
        private val repository: UserPreferencesRepository,
    ) {
        suspend operator fun invoke(option: ThemeOption) = repository.setTheme(option)
    }
