package com.asensiodev.feature.searchmovies.impl.data.datasource

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie

internal interface SearchMoviesDatasource {
    suspend fun searchMovies(
        query: String,
        page: Int,
    ): Result<List<Movie>>

    suspend fun getPopularMovies(page: Int): Result<List<Movie>>
}
