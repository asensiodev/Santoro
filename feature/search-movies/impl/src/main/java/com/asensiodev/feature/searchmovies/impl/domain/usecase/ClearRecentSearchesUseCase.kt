package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.feature.searchmovies.impl.domain.repository.RecentSearchesRepository
import javax.inject.Inject

class ClearRecentSearchesUseCase
    @Inject
    constructor(
        private val repository: RecentSearchesRepository,
    ) {
        suspend operator fun invoke() = repository.clearAll()
    }
