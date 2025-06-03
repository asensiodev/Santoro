package com.asensiodev.feature.watchlist.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SearchWatchlistMoviesUseCase
    @Inject
    constructor(
        private val repository: DatabaseRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        operator fun invoke(query: String) =
            repository.searchWatchlistMoviesByTitle(query).flowOn(dispatchers.io)
    }
