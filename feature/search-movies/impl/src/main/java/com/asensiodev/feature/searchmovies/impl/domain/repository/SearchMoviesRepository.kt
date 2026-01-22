package com.asensiodev.feature.searchmovies.impl.domain.repository

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

internal interface SearchMoviesRepository {
    fun searchMovies(
        query: String,
        page: Int,
    ): Flow<Result<List<Movie>>>

    fun getNowPlayingMovies(page: Int): Flow<Result<List<Movie>>>

    fun getPopularMovies(page: Int): Flow<Result<List<Movie>>>

    fun getTopRatedMovies(page: Int): Flow<Result<List<Movie>>>

    fun getUpcomingMovies(page: Int): Flow<Result<List<Movie>>>
}
