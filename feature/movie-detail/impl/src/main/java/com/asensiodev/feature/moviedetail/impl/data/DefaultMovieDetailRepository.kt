package com.asensiodev.feature.moviedetail.impl.data

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class DefaultMovieDetailRepository
    @Inject
    constructor(
        private val remoteDatasource: MovieDetailDataSource,
    ) : MovieDetailRepository {
        override fun getMovieDetail(id: Int): Flow<Result<Movie>> =
            flow {
                emit(Result.Loading)
                try {
                    val response = remoteDatasource.getMovieDetail(id)
                    if (response is Result.Success) {
                        emit(Result.Success(response.data))
                    } else if (response is Result.Error) {
                        emit(Result.Error(response.exception))
                    }
                } catch (e: IOException) {
                    emit(Result.Error(e))
                } catch (e: HttpException) {
                    emit(Result.Error(e))
                }
            }
    }
