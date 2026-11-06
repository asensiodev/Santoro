package com.asensiodev.feature.watchedmovies.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class GetWatchlistMovieIdsUseCase
    @Inject
    constructor(
        private val repository: DatabaseRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        operator fun invoke(): Flow<Set<Int>> =
            repository
                .getWatchlistMovies()
                .map { result -> result.getOrDefault(emptyList()).map { it.id }.toSet() }
                .flowOn(dispatchers.io)
    }
