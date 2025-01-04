package com.asensiodev.feature.searchmovies.impl.domain.repository

import com.asensiodev.core.domain.MovieDetail
import com.asensiodev.core.domain.Result
import kotlinx.coroutines.flow.Flow

internal interface SearchMoviesRepository {
    fun searchMovies(query: String): Flow<Result<List<MovieDetail>>>
}
