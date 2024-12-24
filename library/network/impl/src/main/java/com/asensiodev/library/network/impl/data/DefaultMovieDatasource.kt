package com.asensiodev.library.network.impl.data

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import com.asensiodev.library.network.impl.data.mapper.toDomain
import com.asensiodev.library.network.impl.data.model.MovieResponseApiModel
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class DefaultMovieDatasource
    @Inject
    constructor(
        private val apiService: MovieApiService,
    ) : MovieDatasource {
        override suspend fun searchMovies(query: String): Result<List<Movie>> =
            try {
                val response: MovieResponseApiModel = apiService.searchMovies(query)
                val movies = response.toDomain()
                Result.Success(movies)
            } catch (e: IOException) {
                Result.Error(e)
            } catch (e: HttpException) {
                Result.Error(e)
            }
    }
