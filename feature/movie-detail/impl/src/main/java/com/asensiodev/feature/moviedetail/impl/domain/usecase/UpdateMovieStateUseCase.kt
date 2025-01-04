package com.asensiodev.feature.moviedetail.impl.domain.usecase

import com.asensiodev.core.domain.MovieDetail
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import javax.inject.Inject

internal class UpdateMovieStateUseCase
    @Inject
    constructor(
        private val repository: MovieDetailRepository,
    ) {
        suspend operator fun invoke(movie: MovieDetail) = repository.updateMovieState(movie)
    }
