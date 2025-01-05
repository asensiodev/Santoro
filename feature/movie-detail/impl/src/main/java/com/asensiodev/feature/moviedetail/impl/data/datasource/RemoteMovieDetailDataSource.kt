package com.asensiodev.feature.moviedetail.impl.data.datasource

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import kotlinx.coroutines.flow.Flow

internal interface RemoteMovieDetailDataSource {
    fun getMovieDetail(id: Int): Flow<Result<Movie?>>
}
