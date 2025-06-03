package com.asensiodev.feature.moviedetail.impl.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class UpdateMovieStateUseCase
    @Inject
    constructor(
        private val repository: MovieDetailRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        suspend operator fun invoke(movie: Movie) =
            withContext(dispatchers.io) {
                repository.updateMovieState(movie)
            }
    }
