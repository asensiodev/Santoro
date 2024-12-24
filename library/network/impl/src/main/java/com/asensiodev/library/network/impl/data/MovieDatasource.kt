package com.asensiodev.library.network.impl.data

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result

internal interface MovieDatasource {
    suspend fun searchMovies(query: String): Result<List<Movie>>
}
