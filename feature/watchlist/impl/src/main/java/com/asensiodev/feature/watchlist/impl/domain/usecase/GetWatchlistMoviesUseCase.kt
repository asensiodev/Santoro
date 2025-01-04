package com.asensiodev.feature.watchlist.impl.domain.usecase

import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import javax.inject.Inject

internal class GetWatchlistMoviesUseCase
    @Inject
    constructor(
        private val repository: DatabaseRepository,
    ) {
        operator fun invoke() = repository.getWatchlistMovies()
    }
