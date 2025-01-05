package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import javax.inject.Inject

internal class GetPopularMoviesUseCase
    @Inject
    constructor(
        private val repository: SearchMoviesRepository,
    ) {
        operator fun invoke() = repository.getPopularMovies()
    }
