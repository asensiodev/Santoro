package com.asensiodev.santoro.core.database.domain

import com.asensiodev.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {
    fun getWatchedMovies(): Flow<Result<List<Movie>>>
    fun getWatchlistMovies(): Flow<Result<List<Movie>>>
    suspend fun getMovieById(movieId: Int): Result<Movie?>
    fun searchWatchedMoviesByTitle(query: String): Flow<Result<List<Movie>>>
    fun searchWatchlistMoviesByTitle(query: String): Flow<Result<List<Movie>>>
    suspend fun updateMovieState(movie: Movie): Result<Boolean>
    suspend fun removeFromWatchlist(movieId: Int): Result<Boolean>
    suspend fun getMoviesForSync(): Result<List<Movie>>
    suspend fun upsertMovieFromSync(
        movieId: Int,
        title: String,
        posterPath: String?,
        isWatched: Boolean,
        isInWatchlist: Boolean,
        watchedAt: Long?,
        updatedAt: Long,
    ): Result<Unit>
    suspend fun updateMovieSyncState(
        movieId: Int,
        isWatched: Boolean,
        isInWatchlist: Boolean,
        watchedAt: Long?,
        updatedAt: Long,
    ): Result<Unit>
    suspend fun clearAllUserData(): Result<Unit>
}
