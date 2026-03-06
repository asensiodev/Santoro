package com.asensiodev.santoro.core.database.data.repository

import android.database.sqlite.SQLiteException
import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
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

class RoomDatabaseRepository
    @Inject
    constructor(
        private val movieDao: MovieDao,
    ) : DatabaseRepository {
        override fun getWatchedMovies(): Flow<Result<List<Movie>>> =
            flow {
                emitAll(
                    movieDao
                        .getWatchedMovies()
                        .map { movies -> Result.Success(movies.map { it.toDomain() }) }
                        .catch { e -> emit(Result.Error(e)) },
                )
            }

        override fun getWatchlistMovies(): Flow<Result<List<Movie>>> =
            flow {
                emitAll(
                    movieDao
                        .getWatchlistMovies()
                        .map { movies -> Result.Success(movies.map { it.toDomain() }) }
                        .catch { e -> emit(Result.Error(e)) },
                )
            }

        override suspend fun getMovieById(movieId: Int): Result<Movie?> =
            try {
                val movie = movieDao.getMovieById(movieId)?.toDomain()
                Result.Success(movie)
            } catch (e: SQLiteException) {
                Result.Error(e)
            } catch (e: Exception) {
                Result.Error(e)
            }

        override fun searchWatchedMoviesByTitle(query: String): Flow<Result<List<Movie>>> =
            flow {
                emitAll(
                    movieDao
                        .searchWatchedMoviesByTitle(query)
                        .map { entities ->
                            Result.Success(entities.map { it.toDomain() })
                        }.catch { e -> emit(Result.Error(e)) },
                )
            }

        override fun searchWatchlistMoviesByTitle(query: String): Flow<Result<List<Movie>>> =
            flow {
                emitAll(
                    movieDao
                        .searchWatchlistMoviesByTitle(query)
                        .map { entities ->
                            Result.Success(entities.map { it.toDomain() })
                        }.catch { e -> emit(Result.Error(e)) },
                )
            }

        override suspend fun updateMovieState(movie: Movie): Result<Boolean> =
            try {
                movieDao.insertOrUpdateMovie(movie.toEntity())
                Result.Success(true)
            } catch (e: SQLiteException) {
                Result.Error(e)
            } catch (e: Exception) {
                Result.Error(e)
            }

        override suspend fun removeFromWatchlist(movieId: Int): Result<Boolean> =
            try {
                movieDao.removeFromWatchlist(movieId)
                Result.Success(true)
            } catch (e: SQLiteException) {
                Result.Error(e)
            } catch (e: Exception) {
                Result.Error(e)
            }

        override suspend fun getMoviesForSync(): Result<List<Movie>> =
            try {
                Result.Success(movieDao.getMoviesForSync().map { it.toDomain() })
            } catch (e: SQLiteException) {
                Result.Error(e)
            } catch (e: Exception) {
                Result.Error(e)
            }

        override suspend fun upsertMovieFromSync(
            movieId: Int,
            title: String,
            posterPath: String?,
            isWatched: Boolean,
            isInWatchlist: Boolean,
            watchedAt: Long?,
            updatedAt: Long,
        ): Result<Unit> =
            try {
                movieDao.upsertMovieFromSync(
                    movieId,
                    title,
                    posterPath,
                    isWatched,
                    isInWatchlist,
                    watchedAt,
                    updatedAt,
                )
                Result.Success(Unit)
            } catch (e: SQLiteException) {
                Result.Error(e)
            } catch (e: Exception) {
                Result.Error(e)
            }

        override suspend fun updateMovieSyncState(
            movieId: Int,
            isWatched: Boolean,
            isInWatchlist: Boolean,
            watchedAt: Long?,
            updatedAt: Long,
        ): Result<Unit> =
            try {
                movieDao.updateMovieSyncState(
                    movieId,
                    isWatched,
                    isInWatchlist,
                    watchedAt,
                    updatedAt,
                )
                Result.Success(Unit)
            } catch (e: SQLiteException) {
                Result.Error(e)
            } catch (e: Exception) {
                Result.Error(e)
            }

        override suspend fun clearAllUserData(): Result<Unit> =
            try {
                movieDao.clearAllUserData()
                Result.Success(Unit)
            } catch (e: SQLiteException) {
                Result.Error(e)
            } catch (e: Exception) {
                Result.Error(e)
            }
    }
