package com.asensiodev.feature.searchmovies.impl.data.service

import com.asensiodev.feature.searchmovies.impl.data.model.SearchMoviesResponseApiModel
import retrofit2.http.GET
import retrofit2.http.Query

internal interface SearchMoviesApiService {
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int,
    ): SearchMoviesResponseApiModel

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int,
    ): SearchMoviesResponseApiModel

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int,
    ): SearchMoviesResponseApiModel

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int,
    ): SearchMoviesResponseApiModel

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int,
    ): SearchMoviesResponseApiModel

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("page") page: Int,
    ): SearchMoviesResponseApiModel

    @GET("discover/movie")
    suspend fun getMoviesByGenre(
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int,
    ): SearchMoviesResponseApiModel
}
