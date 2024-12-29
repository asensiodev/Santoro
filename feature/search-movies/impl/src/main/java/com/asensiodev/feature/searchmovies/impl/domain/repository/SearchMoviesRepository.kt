package com.asensiodev.feature.searchmovies.impl.domain.repository

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface SearchMoviesRepository {
    fun searchMovies(query: String): Flow<Result<List<Movie>>>
}