package com.asensiodev.feature.moviedetail.impl.data.repository

import com.asensiodev.core.domain.MovieDetail
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.moviedetail.impl.data.datasource.LocalMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.data.datasource.RemoteMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal class DefaultMovieDetailRepository
    @Inject
    constructor(
        private val localDataSource: LocalMovieDetailDataSource,
        private val remoteDataSource: RemoteMovieDetailDataSource,
    ) : MovieDetailRepository {
        override fun getMovieDetail(id: Int): Flow<Result<MovieDetail?>> {
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
                                        (localResult as? Result.Success)?.data?.isWatched ?: false,
                                    isInWatchlist =
                                        (localResult as? Result.Success)?.data?.isInWatchlist
                                            ?: false,
                                ),
                            )
                        } else {
                            Result.Error(Exception("Remote movie not found"))
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

                    else -> {
                        Result.Error(Exception("Unexpected state"))
                    }
                }
            }
        }

        override suspend fun updateMovieState(movie: MovieDetail): Boolean =
            localDataSource.updateMovieState(movie)
    }
