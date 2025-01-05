package com.asensiodev.feature.searchmovies.impl.data

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result

internal interface SearchMoviesDatasource {
    suspend fun searchMovies(query: String): Result<List<Movie>>
    suspend fun getPopularMovies(): Result<List<Movie>>
}
