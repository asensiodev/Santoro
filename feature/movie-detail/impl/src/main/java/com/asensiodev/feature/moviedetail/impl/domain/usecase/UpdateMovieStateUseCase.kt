package com.asensiodev.feature.moviedetail.impl.domain.usecase

import com.asensiodev.core.domain.Movie
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import javax.inject.Inject

internal class UpdateMovieStateUseCase
    @Inject
    constructor(
        private val repository: MovieDetailRepository,
    ) {
        suspend operator fun invoke(movie: Movie) = repository.updateMovieState(movie)
    }
