package com.asensiodev.santoro.core.database.domain

import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {
    fun getWatchedMovies(): Flow<Result<List<Movie>>>
    fun getWatchlistMovies(): Flow<Result<List<Movie>>>
    fun getMovieById(movieId: Int): Flow<Result<Movie?>>
    suspend fun updateMovieState(movie: Movie): Boolean
}
