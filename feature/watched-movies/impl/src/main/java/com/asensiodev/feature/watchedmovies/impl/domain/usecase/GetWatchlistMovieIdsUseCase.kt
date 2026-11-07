package com.asensiodev.feature.watchedmovies.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
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
                .mapNotNull { result -> result.getOrNull()?.map { it.id }?.toSet() }
                .flowOn(dispatchers.io)
    }
