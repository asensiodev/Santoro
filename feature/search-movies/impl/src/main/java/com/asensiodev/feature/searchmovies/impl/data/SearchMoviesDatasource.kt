package com.asensiodev.feature.searchmovies.impl.data

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result

internal interface SearchMoviesDatasource {
    suspend fun searchMovies(
        query: String,
        page: Int,
    ): Result<List<Movie>>

    suspend fun getPopularMovies(page: Int): Result<List<Movie>>
}
