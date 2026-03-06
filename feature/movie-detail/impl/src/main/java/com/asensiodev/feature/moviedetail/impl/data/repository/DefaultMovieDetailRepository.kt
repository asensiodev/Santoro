package com.asensiodev.feature.moviedetail.impl.data.repository

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.moviedetail.impl.data.datasource.LocalMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.data.datasource.RemoteMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class MovieNotFoundException : Exception()
class UnexpectedErrorException : Exception()

internal class DefaultMovieDetailRepository
    @Inject
    constructor(
        private val localDataSource: LocalMovieDetailDataSource,
        private val remoteDataSource: RemoteMovieDetailDataSource,
    ) : MovieDetailRepository {
        override fun getMovieDetail(id: Int): Flow<Result<Movie?>> {
            val localFlow = localDataSource.getMovieDetail(id)
            val remoteFlow = remoteDataSource.getMovieDetail(id)

            return combine(localFlow, remoteFlow) { localResult, remoteResult ->
                remoteResult.fold(
                    onSuccess = { remoteMovie ->
                        if (remoteMovie != null) {
                            Result.success(
                                remoteMovie.copy(
                                    isWatched = localResult.getOrNull()?.isWatched ?: false,
                                    isInWatchlist = localResult.getOrNull()?.isInWatchlist ?: false,
                                ),
                            )
                        } else {
                            Result.failure(MovieNotFoundException())
                        }
                    },
                    onFailure = { exception ->
                        val localMovie = localResult.getOrNull()
                        if (localMovie != null) {
                            Result.success(localMovie)
                        } else {
                            Result.failure(exception)
                        }
                    },
                )
            }.catch { emit(Result.failure(UnexpectedErrorException())) }
        }

        override suspend fun updateMovieState(movie: Movie): Result<Boolean> =
            localDataSource.updateMovieState(movie)
    }
