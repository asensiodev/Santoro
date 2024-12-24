package com.asensiodev.library.network.impl.data

import com.asensiodev.library.network.impl.data.model.MovieResponseApiModel
import retrofit2.http.GET
import retrofit2.http.Query

internal interface MovieApiService {
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
    ): MovieResponseApiModel
}
