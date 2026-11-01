package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.core.domain.model.Director

internal interface MovieMetadataRemoteDataSource {
    suspend fun getMetadata(movieId: Int): Result<RemoteMovieMetadata>
}

internal data class RemoteMovieMetadata(
    val directors: List<Director>,
    val releaseDate: String,
)
