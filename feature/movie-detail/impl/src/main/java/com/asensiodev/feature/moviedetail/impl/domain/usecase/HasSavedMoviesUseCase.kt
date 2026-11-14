package com.asensiodev.feature.moviedetail.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class HasSavedMoviesUseCase
    @Inject
    constructor(
        private val repository: DatabaseRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        suspend operator fun invoke(): Boolean =
            withContext(dispatchers.io) {
                repository.getWatchedMovies().hasAny() || repository.getWatchlistMovies().hasAny()
            }

        private suspend fun Flow<Result<List<Movie>>>.hasAny(): Boolean =
            first().getOrNull().orEmpty().isNotEmpty()
    }
