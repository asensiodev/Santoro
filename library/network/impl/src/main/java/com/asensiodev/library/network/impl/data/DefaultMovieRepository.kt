package com.asensiodev.library.network.impl.data

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import com.asensiodev.library.network.api.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class DefaultMovieRepository
    @Inject
    constructor(
        private val remoteDatasource: MovieDatasource,
    ) : MovieRepository {
        override fun searchMovies(query: String): Flow<Result<List<Movie>>> =
            flow {
                emit(Result.Loading)
                try {
                    val response = remoteDatasource.searchMovies(query)
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
