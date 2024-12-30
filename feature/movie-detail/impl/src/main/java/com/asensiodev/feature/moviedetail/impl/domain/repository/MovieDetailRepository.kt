package com.asensiodev.feature.moviedetail.impl.domain.repository

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import kotlinx.coroutines.flow.Flow

internal interface MovieDetailRepository {
    fun getMovieDetail(id: Int): Flow<Result<Movie>>
}
