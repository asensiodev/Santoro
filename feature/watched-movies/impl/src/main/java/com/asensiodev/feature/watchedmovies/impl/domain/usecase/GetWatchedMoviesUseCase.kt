package com.asensiodev.feature.watchedmovies.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

internal class GetWatchedMoviesUseCase
    @Inject
    constructor(
        private val repository: DatabaseRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        operator fun invoke() = repository.getWatchedMovies().flowOn(dispatchers.io)
    }
