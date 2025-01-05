package com.asensiodev.santoro.core.database.data

import android.util.Log
import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import com.asensiodev.santoro.core.database.data.dao.MovieDao
import com.asensiodev.santoro.core.database.data.mapper.toDomain
import com.asensiodev.santoro.core.database.data.mapper.toEntity
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DatabaseRepositoryImpl
    @Inject
    constructor(
        private val movieDao: MovieDao,
    ) : DatabaseRepository {
        override fun getWatchedMovies(): Flow<Result<List<Movie>>> =
            flow {
                emit(Result.Loading)
                emitAll(
                    movieDao
                        .getWatchedMovies()
                        .map { movies -> Result.Success(movies.map { it.toDomain() }) }
                        .catch { e -> emit(Result.Error(e)) },
                )
            }

        override fun getWatchlistMovies(): Flow<Result<List<Movie>>> =
            flow {
                emit(Result.Loading)
                emitAll(
                    movieDao
                        .getWatchlistMovies()
                        .map { movies -> Result.Success(movies.map { it.toDomain() }) }
                        .catch { e -> emit(Result.Error(e)) },
                )
            }

        override fun getMovieById(movieId: Int): Flow<Result<Movie?>> =
            flow {
                emit(Result.Loading)
                try {
                    val movie = movieDao.getMovieById(movieId)?.toDomain()
                    emit(Result.Success(movie))
                } catch (e: Exception) {
                    emit(Result.Error(e))
                }
            }

        override suspend fun updateMovieState(movie: Movie): Boolean =
            try {
                movieDao.insertOrUpdateMovie(movie.toEntity())
                true
            } catch (e: Exception) {
                Log.d("TAG", e.message ?: "Unknown error")
                false
            }
    }
