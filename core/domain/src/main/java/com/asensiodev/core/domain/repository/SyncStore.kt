package com.asensiodev.core.domain.repository

import com.asensiodev.core.domain.model.MovieSyncData

interface SyncStore {
    suspend fun getMovieForUpload(movieId: Int): Result<MovieSyncData?>

    suspend fun getMoviesForUpload(): Result<List<MovieSyncData>>

    suspend fun completeDownloadedMerge(
        movies: List<MovieSyncData>,
        canMerge: suspend () -> Boolean,
    ): Result<Unit>
}
