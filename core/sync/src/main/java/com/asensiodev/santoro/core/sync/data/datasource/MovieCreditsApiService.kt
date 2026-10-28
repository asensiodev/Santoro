package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.santoro.core.data.model.CreditsApiModel
import retrofit2.http.GET
import retrofit2.http.Path

internal interface MovieCreditsApiService {
    @GET("movie/{movie_id}/credits")
    suspend fun credits(
        @Path("movie_id") movieId: Int,
    ): CreditsApiModel
}
