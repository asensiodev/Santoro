package com.asensiodev.feature.searchmovies.impl.data.datasource

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie

internal interface SearchMoviesDatasource {
    suspend fun searchMovies(
        query: String,
        page: Int,
    ): Result<List<Movie>>

    suspend fun getNowPlayingMovies(page: Int): Result<List<Movie>>

    suspend fun getTopRatedMovies(page: Int): Result<List<Movie>>

    suspend fun getPopularMovies(page: Int): Result<List<Movie>>

    suspend fun getUpcomingMovies(page: Int): Result<List<Movie>>

    suspend fun getTrendingMovies(page: Int): Result<List<Movie>>

    suspend fun getMoviesByGenre(
        genreId: Int,
        page: Int,
    ): Result<List<Movie>>
}
