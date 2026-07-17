package com.asensiodev.feature.moviedetail.impl.data.repository

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.result.rethrowCancellation
import com.asensiodev.feature.moviedetail.impl.data.datasource.LocalMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.data.datasource.RemoteMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class MovieNotFoundException : Exception()
class UnexpectedErrorException(
    cause: Throwable,
) : Exception(cause)

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
                val localMovie = localResult.rethrowCancellation().getOrNull()
                remoteResult.rethrowCancellation().fold(
                    onSuccess = { remoteMovie ->
                        if (remoteMovie != null) {
                            Result.success(
                                remoteMovie.copy(
                                    isWatched = localMovie?.isWatched ?: false,
                                    isInWatchlist = localMovie?.isInWatchlist ?: false,
                                    watchedAt = localMovie?.watchedAt,
                                ),
                            )
                        } else {
                            Result.failure(MovieNotFoundException())
                        }
                    },
                    onFailure = { exception ->
                        if (localMovie != null) {
                            Result.success(localMovie)
                        } else {
                            Result.failure(exception)
                        }
                    },
                )
            }.catch { exception ->
                if (exception is CancellationException) throw exception
                emit(Result.failure(UnexpectedErrorException(exception)))
            }
        }

        override suspend fun updateMovieState(movie: Movie): Result<Boolean> =
            localDataSource.updateMovieState(movie)
    }
