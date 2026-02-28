package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.feature.searchmovies.impl.domain.repository.RecentSearchesRepository
import javax.inject.Inject

private const val MAX_RECENT_SEARCHES = 5

class SaveRecentSearchUseCase
    @Inject
    constructor(
        private val repository: RecentSearchesRepository,
    ) {
        suspend operator fun invoke(query: String) {
            val trimmed = query.trim()
            if (trimmed.isBlank()) return
            repository.saveSearch(trimmed)
        }

        companion object {
            const val MAX_ENTRIES = MAX_RECENT_SEARCHES
        }
    }
