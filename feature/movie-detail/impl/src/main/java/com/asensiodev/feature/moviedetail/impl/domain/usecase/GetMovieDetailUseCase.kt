package com.asensiodev.feature.moviedetail.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

internal class GetMovieDetailUseCase
    @Inject
    constructor(
        private val repository: MovieDetailRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        operator fun invoke(id: Int) = repository.getMovieDetail(id).flowOn(dispatchers.io)
    }
