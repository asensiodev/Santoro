package com.asensiodev.feature.moviedetail.impl.data.datasource

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

internal interface LocalMovieDetailDataSource {
    fun getMovieDetail(id: Int): Flow<Result<Movie?>>
    suspend fun updateMovieState(movie: Movie): Result<Boolean>
}
