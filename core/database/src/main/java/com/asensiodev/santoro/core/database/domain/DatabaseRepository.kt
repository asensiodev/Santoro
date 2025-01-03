package com.asensiodev.santoro.core.database.domain

import com.asensiodev.core.domain.Movie

interface DatabaseRepository {
    suspend fun getWatchlistMovies(): List<Movie>
    suspend fun getWatchedMovies(): List<Movie>
    suspend fun getMovieById(movieId: Int): Movie?
    suspend fun saveMovie(
        movie: Movie,
        isWatched: Boolean = false,
        isInWatchlist: Boolean = false,
    )
}
