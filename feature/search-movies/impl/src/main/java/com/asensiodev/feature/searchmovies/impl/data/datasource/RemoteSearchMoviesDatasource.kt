package com.asensiodev.feature.searchmovies.impl.data.datasource

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
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
        override suspend fun searchMovies(
            query: String,
            page: Int,
        ): Result<List<Movie>> =
            try {
                val response: SearchMoviesResponseApiModel =
                    apiService
                        .searchMovies(
                            query,
                            page,
                        )
                val movies = response.toDomain()
                Result.Success(movies)
            } catch (e: IOException) {
                Result.Error(e)
            } catch (e: HttpException) {
                Result.Error(e)
            }

        override suspend fun getPopularMovies(page: Int): Result<List<Movie>> =
            try {
                val response: SearchMoviesResponseApiModel = apiService.getPopularMovies(page)
                val movies = response.toDomain()
                Result.Success(movies)
            } catch (e: IOException) {
                Result.Error(e)
            } catch (e: HttpException) {
                Result.Error(e)
            }
    }
