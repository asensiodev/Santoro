package com.asensiodev.feature.watchedmovies.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddWatchedMovieUseCase
    @Inject
    constructor(
        private val databaseRepository: DatabaseRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        suspend operator fun invoke(movie: Movie) =
            withContext(dispatchers.io) {
                databaseRepository.updateMovieState(movie)
            }
    }
