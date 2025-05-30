package com.asensiodev.feature.watchedmovies.impl.domain.usecase

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchWatchedMoviesUseCase
    @Inject
    constructor(
        private val repository: DatabaseRepository,
    ) {
        operator fun invoke(query: String): Flow<Result<List<Movie>>> =
            repository.searchWatchedMoviesByTitle(query)
    }
