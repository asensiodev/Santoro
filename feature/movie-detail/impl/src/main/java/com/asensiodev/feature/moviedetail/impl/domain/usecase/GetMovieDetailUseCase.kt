package com.asensiodev.feature.moviedetail.impl.domain.usecase

import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import javax.inject.Inject

internal class GetMovieDetailUseCase
    @Inject
    constructor(
        private val repository: MovieDetailRepository,
    ) {
        operator fun invoke(id: Int) = repository.getMovieDetail(id)
    }
