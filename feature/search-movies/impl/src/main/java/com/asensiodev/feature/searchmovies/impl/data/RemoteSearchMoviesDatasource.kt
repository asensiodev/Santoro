package com.asensiodev.feature.searchmovies.impl.data

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.searchmovies.impl.data.mapper.toDomain
import com.asensiodev.feature.searchmovies.impl.data.model.SearchMoviesResponseApiModel
import com.asensiodev.feature.searchmovies.impl.data.service.SearchMoviesApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class RemoteSearchMoviesDatasource
    @Inject
    constructor(
        private val apiService: SearchMoviesApiService,
    ) : SearchMoviesDatasource {
        override suspend fun searchMovies(query: String): Result<List<Movie>> =
            try {
                val response: SearchMoviesResponseApiModel =
                    apiService
                        .searchMovies(
                            query,
                        )
                val movies = response.toDomain()
                Result.Success(movies)
            } catch (e: IOException) {
                Result.Error(e)
            } catch (e: HttpException) {
                Result.Error(e)
            }

        override suspend fun getPopularMovies(): Result<List<Movie>> =
            try {
                val response: SearchMoviesResponseApiModel =
                    apiService.getPopularMovies()
                val movies = response.toDomain()
                Result.Success(movies)
            } catch (e: IOException) {
                Result.Error(e)
            } catch (e: HttpException) {
                Result.Error(e)
            }
    }
