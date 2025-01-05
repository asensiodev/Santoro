package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import javax.inject.Inject

internal class SearchMoviesUseCase
    @Inject
    constructor(
        private val repository: SearchMoviesRepository,
    ) {
        operator fun invoke(
            query: String,
            page: Int,
        ) = repository.searchMovies(query, page)
    }
