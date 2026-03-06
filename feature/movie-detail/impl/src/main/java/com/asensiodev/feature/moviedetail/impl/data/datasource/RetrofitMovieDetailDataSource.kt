package com.asensiodev.feature.moviedetail.impl.data.datasource

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.moviedetail.impl.data.service.MovieDetailApiService
import com.asensiodev.santoro.core.data.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class RetrofitMovieDetailDataSource
    @Inject
    constructor(
        private val apiService: MovieDetailApiService,
    ) : RemoteMovieDetailDataSource {
        override fun getMovieDetail(id: Int): Flow<Result<Movie?>> =
            flow {
                try {
                    val movie = apiService.movieDetail(id).toDomain()
                    emit(Result.success(movie))
                } catch (e: IOException) {
                    emit(Result.failure(e))
                } catch (e: HttpException) {
                    emit(Result.failure(e))
                }
            }
    }
