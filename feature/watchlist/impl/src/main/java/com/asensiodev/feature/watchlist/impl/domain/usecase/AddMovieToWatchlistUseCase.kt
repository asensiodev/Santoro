package com.asensiodev.feature.watchlist.impl.domain.usecase

import com.asensiodev.core.domain.MovieDetail
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import javax.inject.Inject

class AddMovieToWatchlistUseCase
    @Inject
    constructor(
        private val databaseRepository: DatabaseRepository,
    ) {
        suspend operator fun invoke(movie: MovieDetail) {
            databaseRepository.updateMovieState(movie)
        }
    }
