package com.asensiodev.feature.searchmovies.impl.data.service

import com.asensiodev.feature.searchmovies.impl.data.model.SearchMoviesResponseApiModel
import retrofit2.http.GET
import retrofit2.http.Query

internal interface SearchMoviesApiService {
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
    ): SearchMoviesResponseApiModel
}
