package com.asensiodev.santoro.core.database.domain

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {
    fun getWatchedMovies(): Flow<Result<List<Movie>>>
    fun getWatchlistMovies(): Flow<Result<List<Movie>>>
    suspend fun getMovieById(movieId: Int): Result<Movie?>
    fun searchWatchedMoviesByTitle(query: String): Flow<Result<List<Movie>>>
    fun searchWatchlistMoviesByTitle(query: String): Flow<Result<List<Movie>>>
    suspend fun updateMovieState(movie: Movie): Result<Boolean>
}
