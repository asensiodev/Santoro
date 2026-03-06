package com.asensiodev.feature.moviedetail.impl.data.datasource

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal class RoomMovieDetailDataSource
    @Inject
    constructor(
        private val databaseRepository: DatabaseRepository,
    ) : LocalMovieDetailDataSource {
        override fun getMovieDetail(id: Int): Flow<Result<Movie?>> =
            flow {
                emit(databaseRepository.getMovieById(id))
            }

        override suspend fun updateMovieState(movie: Movie): Result<Boolean> =
            databaseRepository.updateMovieState(movie)
    }
