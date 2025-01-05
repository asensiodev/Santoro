package com.asensiodev.feature.watchedmovies.impl.domain.usecase

import com.asensiodev.core.domain.Movie
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import javax.inject.Inject

class AddWatchedMovieUseCase
    @Inject
    constructor(
        private val databaseRepository: DatabaseRepository,
    ) {
        suspend operator fun invoke(movie: Movie) {
            databaseRepository.updateMovieState(movie)
        }
    }
