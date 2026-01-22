package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

internal class GetUpcomingMoviesUseCase
    @Inject
    constructor(
        private val repository: SearchMoviesRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        operator fun invoke(page: Int) = repository.getUpcomingMovies(page).flowOn(dispatchers.io)
    }
