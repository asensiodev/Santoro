package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.feature.searchmovies.impl.domain.model.FetchPolicy
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

internal class GetTrendingMoviesUseCase
    @Inject
    constructor(
        private val repository: SearchMoviesRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        operator fun invoke(page: Int) = fetch(page, FetchPolicy.CACHE_FIRST)

        fun refresh(page: Int) = fetch(page, FetchPolicy.REFRESH)

        private fun fetch(
            page: Int,
            fetchPolicy: FetchPolicy,
        ) = repository.getTrendingMovies(page, fetchPolicy).flowOn(dispatchers.io)
    }
