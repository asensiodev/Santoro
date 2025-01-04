package com.asensiodev.feature.watchedmovies.impl.domain.usecase

import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import javax.inject.Inject

internal class GetWatchedMoviesUseCase
    @Inject
    constructor(
        private val repository: DatabaseRepository,
    ) {
        operator fun invoke() = repository.getWatchedMovies()
    }
