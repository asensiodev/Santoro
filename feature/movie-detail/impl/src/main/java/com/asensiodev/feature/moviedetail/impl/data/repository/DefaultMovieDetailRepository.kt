package com.asensiodev.feature.moviedetail.impl.data.repository

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
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
                when (remoteResult) {
                    is Result.Success -> {
                        val remoteMovie = remoteResult.data
                        if (remoteMovie != null) {
                            Result.Success(
                                remoteMovie.copy(
                                    isWatched =
                                        (localResult as? Result.Success)?.data?.isWatched
                                            ?: false,
                                    isInWatchlist =
                                        (localResult as? Result.Success)?.data?.isInWatchlist
                                            ?: false,
                                ),
                            )
                        } else {
                            Result.Error(MovieNotFoundException())
                        }
                    }

                    is Result.Loading -> {
                        if (localResult is Result.Success && localResult.data != null) {
                            Result.Success(localResult.data)
                        } else {
                            Result.Loading
                        }
                    }

                    is Result.Error -> {
                        if (localResult is Result.Success && localResult.data != null) {
                            Result.Success(localResult.data)
                        } else {
                            Result.Error(remoteResult.exception)
                        }
                    }
                }
            }.catch { emit(Result.Error(UnexpectedErrorException())) }
        }

        override suspend fun updateMovieState(movie: Movie): Result<Boolean> =
            localDataSource.updateMovieState(movie)
    }
