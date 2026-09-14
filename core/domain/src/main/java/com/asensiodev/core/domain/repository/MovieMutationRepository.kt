package com.asensiodev.core.domain.repository

import com.asensiodev.core.domain.model.Movie

interface MovieMutationRepository {
    suspend fun updateMovieState(movie: Movie): Result<Unit>

    suspend fun removeFromWatchlist(movieId: Int): Result<Unit>

    suspend fun clearMovies()
}
