package com.asensiodev.feature.watchlist.impl.domain.usecase

import com.asensiodev.core.domain.Movie
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import javax.inject.Inject

class AddMovieToWatchlistUseCase
    @Inject
    constructor(
        private val databaseRepository: DatabaseRepository,
    ) {
        suspend operator fun invoke(movie: Movie) {
            databaseRepository.updateMovieState(movie)
        }
    }
