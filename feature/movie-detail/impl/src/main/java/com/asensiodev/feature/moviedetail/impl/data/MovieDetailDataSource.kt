package com.asensiodev.feature.moviedetail.impl.data

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result

internal interface MovieDetailDataSource {
    suspend fun getMovieDetail(id: Int): Result<Movie>
}
