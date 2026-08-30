package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.santoro.core.sync.data.model.MovieSyncEntity

internal interface MovieSyncRemoteDataSource {
    suspend fun uploadMovie(
        uid: String,
        entity: MovieSyncEntity,
    ): Result<Unit>
    suspend fun uploadMovies(
        uid: String,
        entities: List<MovieSyncEntity>,
    ): Result<Unit>
    suspend fun downloadUserMovies(uid: String): Result<List<MovieSyncEntity>>
    suspend fun deleteUserData(uid: String): Result<Unit>
}
