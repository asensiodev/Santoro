package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.feature.searchmovies.impl.domain.model.FetchPolicy
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

internal class SearchMoviesUseCase
    @Inject
    constructor(
        private val repository: SearchMoviesRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        operator fun invoke(
            query: String,
            page: Int,
        ) = fetch(query, page, FetchPolicy.CACHE_FIRST)

        fun refresh(
            query: String,
            page: Int,
        ) = fetch(query, page, FetchPolicy.REFRESH)

        private fun fetch(
            query: String,
            page: Int,
            fetchPolicy: FetchPolicy,
        ) = repository.searchMovies(query, page, fetchPolicy).flowOn(dispatchers.io)
    }
