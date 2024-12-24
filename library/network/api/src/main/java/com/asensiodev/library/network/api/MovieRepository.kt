package com.asensiodev.library.network.api

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun searchMovies(query: String): Flow<Result<List<Movie>>>
}
