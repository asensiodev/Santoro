package com.asensiodev.feature.searchmovies.impl.data.repository

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.data.datasource.SearchMoviesDatasource
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class RemoteSearchMoviesRepository
    @Inject
    constructor(
        private val remoteDatasource: SearchMoviesDatasource,
    ) : SearchMoviesRepository {
        override fun searchMovies(
            query: String,
            page: Int,
        ): Flow<Result<List<Movie>>> =
            flow {
                try {
                    val response = remoteDatasource.searchMovies(query, page)
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

        override fun getNowPlayingMovies(page: Int): Flow<Result<List<Movie>>> =
            flow {
                try {
                    val response = remoteDatasource.getNowPlayingMovies(page)
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

        override fun getPopularMovies(page: Int): Flow<Result<List<Movie>>> =
            flow {
                try {
                    val response = remoteDatasource.getPopularMovies(page)
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

        override fun getTopRatedMovies(page: Int): Flow<Result<List<Movie>>> =
            flow {
                try {
                    val response = remoteDatasource.getTopRatedMovies(page)
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

        override fun getUpcomingMovies(page: Int): Flow<Result<List<Movie>>> =
            flow {
                try {
                    val response = remoteDatasource.getUpcomingMovies(page)
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

        override fun getTrendingMovies(page: Int): Flow<Result<List<Movie>>> =
            flow {
                try {
                    val response = remoteDatasource.getTrendingMovies(page)
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

        override fun getMoviesByGenre(
            genreId: Int,
            page: Int,
        ): Flow<Result<List<Movie>>> =
            flow {
                try {
                    val response = remoteDatasource.getMoviesByGenre(genreId, page)
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
