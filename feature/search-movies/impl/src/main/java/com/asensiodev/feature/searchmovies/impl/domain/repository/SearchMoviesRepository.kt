package com.asensiodev.feature.searchmovies.impl.domain.repository

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.domain.model.FetchPolicy
import kotlinx.coroutines.flow.Flow

internal interface SearchMoviesRepository {
    fun searchMovies(
        query: String,
        page: Int,
        fetchPolicy: FetchPolicy = FetchPolicy.CACHE_FIRST,
    ): Flow<Result<List<Movie>>>

    fun getNowPlayingMovies(
        page: Int,
        fetchPolicy: FetchPolicy = FetchPolicy.CACHE_FIRST,
    ): Flow<Result<List<Movie>>>

    fun getPopularMovies(
        page: Int,
        fetchPolicy: FetchPolicy = FetchPolicy.CACHE_FIRST,
    ): Flow<Result<List<Movie>>>

    fun getTopRatedMovies(
        page: Int,
        fetchPolicy: FetchPolicy = FetchPolicy.CACHE_FIRST,
    ): Flow<Result<List<Movie>>>

    fun getUpcomingMovies(
        page: Int,
        fetchPolicy: FetchPolicy = FetchPolicy.CACHE_FIRST,
    ): Flow<Result<List<Movie>>>

    fun getTrendingMovies(
        page: Int,
        fetchPolicy: FetchPolicy = FetchPolicy.CACHE_FIRST,
    ): Flow<Result<List<Movie>>>

    fun getMoviesByGenre(
        genreId: Int,
        page: Int,
    ): Flow<Result<List<Movie>>>
}
