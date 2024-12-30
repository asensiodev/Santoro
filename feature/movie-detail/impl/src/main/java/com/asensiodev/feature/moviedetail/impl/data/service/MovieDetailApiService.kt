package com.asensiodev.feature.moviedetail.impl.data.service

import com.asensiodev.santoro.core.data.model.MovieApiModel
import retrofit2.http.GET
import retrofit2.http.Path

internal interface MovieDetailApiService {
    @GET("movie/{movie_id}")
    suspend fun movieDetail(
        @Path("movie_id") movieId: Int,
    ): MovieApiModel
}
