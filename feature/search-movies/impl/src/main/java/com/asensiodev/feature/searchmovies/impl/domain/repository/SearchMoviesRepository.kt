package com.asensiodev.feature.searchmovies.impl.domain.repository

import com.asensiodev.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

internal interface SearchMoviesRepository {
    fun searchMovies(
        query: String,
        page: Int,
        forceRefresh: Boolean = false,
    ): Flow<Result<List<Movie>>>

    fun getNowPlayingMovies(
        page: Int,
        forceRefresh: Boolean = false,
    ): Flow<Result<List<Movie>>>

    fun getPopularMovies(
        page: Int,
        forceRefresh: Boolean = false,
    ): Flow<Result<List<Movie>>>

    fun getTopRatedMovies(
        page: Int,
        forceRefresh: Boolean = false,
    ): Flow<Result<List<Movie>>>

    fun getUpcomingMovies(
        page: Int,
        forceRefresh: Boolean = false,
    ): Flow<Result<List<Movie>>>

    fun getTrendingMovies(
        page: Int,
        forceRefresh: Boolean = false,
    ): Flow<Result<List<Movie>>>

    fun getMoviesByGenre(
        genreId: Int,
        page: Int,
    ): Flow<Result<List<Movie>>>
}
