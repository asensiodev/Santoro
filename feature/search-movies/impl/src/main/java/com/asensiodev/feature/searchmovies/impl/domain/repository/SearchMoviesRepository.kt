package com.asensiodev.feature.searchmovies.impl.domain.repository

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import kotlinx.coroutines.flow.Flow

internal interface SearchMoviesRepository {
    fun searchMovies(
        query: String,
        page: Int,
    ): Flow<Result<List<Movie>>>
    fun getPopularMovies(page: Int): Flow<Result<List<Movie>>>
}
