package com.asensiodev.core.domain.repository

import com.asensiodev.core.domain.model.Director
import com.asensiodev.core.domain.model.Movie

interface MovieMutationRepository {
    suspend fun updateMovieState(movie: Movie): Result<Unit>

    suspend fun removeFromWatchlist(movieId: Int): Result<Unit>

    suspend fun storeResolvedMetadata(
        movieId: Int,
        directors: List<Director>?,
        releaseDate: String?,
    ): Result<Boolean>

    suspend fun clearMovies()
}
