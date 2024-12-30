package com.asensiodev.feature.moviedetail.impl.data

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.moviedetail.impl.data.service.MovieDetailApiService
import com.asensiodev.santoro.core.data.mapper.toDomain
import com.asensiodev.santoro.core.data.model.MovieApiModel
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class DefaultMovieDetailDataSource
    @Inject
    constructor(
        private val apiService: MovieDetailApiService,
    ) : MovieDetailDataSource {
        override suspend fun getMovieDetail(id: Int): Result<Movie> =
            try {
                val response: MovieApiModel =
                    apiService
                        .movieDetail(
                            id,
                        )
                val movie = response.toDomain()
                Result.Success(movie)
            } catch (e: IOException) {
                Result.Error(e)
            } catch (e: HttpException) {
                Result.Error(e)
            }
    }
