package com.asensiodev.santoro.core.database.data

import com.asensiodev.core.domain.Movie
import com.asensiodev.santoro.core.database.data.mapper.toDomain
import com.asensiodev.santoro.core.database.data.mapper.toEntity
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import javax.inject.Inject

class DatabaseRepositoryImpl
    @Inject
    constructor(
        private val movieDao: MovieDao,
    ) : DatabaseRepository {
        override suspend fun getWatchlistMovies(): List<Movie> =
            movieDao.getWatchlistMovies().map { it.toDomain() }

        override suspend fun getWatchedMovies(): List<Movie> =
            movieDao.getWatchedMovies().map { it.toDomain() }

        override suspend fun getMovieById(movieId: Int): Movie? =
            movieDao.getMovieById(movieId)?.toDomain()

        override suspend fun saveMovie(
            movie: Movie,
            isWatched: Boolean,
            isInWatchlist: Boolean,
        ) {
            val movieEntity =
                movie.toEntity().copy(
                    isWatched = isWatched,
                    isInWatchlist = isInWatchlist,
                )
            movieDao.insertOrUpdateMovie(movieEntity)
        }
    }
