package com.asensiodev.santoro.core.database.domain

import com.asensiodev.core.domain.MovieDetail
import com.asensiodev.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {
    fun getWatchedMovies(): Flow<Result<List<MovieDetail>>>
    fun getWatchlistMovies(): Flow<Result<List<MovieDetail>>>
    fun getMovieById(movieId: Int): Flow<Result<MovieDetail?>>
    suspend fun updateMovieState(movie: MovieDetail): Boolean
}
