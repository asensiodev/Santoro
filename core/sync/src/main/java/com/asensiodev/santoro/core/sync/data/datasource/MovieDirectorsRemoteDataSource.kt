package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.core.domain.model.Director

internal interface MovieDirectorsRemoteDataSource {
    suspend fun getDirectors(movieId: Int): Result<List<Director>>
}
