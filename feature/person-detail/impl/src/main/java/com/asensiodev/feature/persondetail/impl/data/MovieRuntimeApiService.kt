package com.asensiodev.feature.persondetail.impl.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

internal interface MovieRuntimeApiService {
    @GET("movie/{movie_id}")
    suspend fun movie(
        @Path("movie_id") id: Int,
    ): MovieRuntimeApiModel
}

internal data class MovieRuntimeApiModel(
    @SerializedName("id") val id: Int?,
    @SerializedName("runtime") val runtime: Int?,
)
