package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.santoro.core.data.model.MovieApiModel
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface MovieMetadataApiService {
    @GET("movie/{movie_id}")
    suspend fun movieDetail(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits",
    ): MovieApiModel
}
