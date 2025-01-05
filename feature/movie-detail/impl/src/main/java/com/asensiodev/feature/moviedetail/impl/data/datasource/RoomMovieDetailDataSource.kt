package com.asensiodev.feature.moviedetail.impl.data.datasource

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class RoomMovieDetailDataSource
    @Inject
    constructor(
        private val databaseRepository: DatabaseRepository,
    ) : LocalMovieDetailDataSource {
        override fun getMovieDetail(id: Int): Flow<Result<Movie?>> =
            flow {
                emit(Result.Loading)
                emitAll(
                    databaseRepository.getMovieById(id).map { result ->
                        when (result) {
                            is Result.Success -> Result.Success(result.data)
                            is Result.Error -> Result.Error(result.exception)
                            else -> Result.Error(Exception("Unexpected state"))
                        }
                    },
                )
            }

        override suspend fun updateMovieState(movie: Movie): Boolean =
            databaseRepository.updateMovieState(movie)
    }
