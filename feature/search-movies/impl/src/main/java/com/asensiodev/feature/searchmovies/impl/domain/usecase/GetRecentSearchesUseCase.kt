package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.feature.searchmovies.impl.domain.repository.RecentSearchesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentSearchesUseCase
    @Inject
    constructor(
        private val repository: RecentSearchesRepository,
    ) {
        operator fun invoke(): Flow<List<String>> = repository.getRecentSearches()
    }
