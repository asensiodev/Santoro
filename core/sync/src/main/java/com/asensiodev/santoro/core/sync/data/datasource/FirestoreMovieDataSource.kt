package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.santoro.core.sync.data.model.MovieSyncEntity

internal interface FirestoreMovieDataSource {
    suspend fun uploadMovie(
        uid: String,
        entity: MovieSyncEntity,
    ): Result<Unit>
    suspend fun downloadUserMovies(uid: String): Result<List<MovieSyncEntity>>
}
