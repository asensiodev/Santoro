package com.asensiodev.santoro.core.database.data.repository

import android.database.sqlite.SQLiteException
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
                        .map { movies -> Result.success(movies.map { it.toDomain() }) }
                        .catch { e -> emit(Result.failure(e)) },
                )
            }

        override fun getWatchlistMovies(): Flow<Result<List<Movie>>> =
            flow {
                emitAll(
                    movieDao
                        .getWatchlistMovies()
                        .map { movies -> Result.success(movies.map { it.toDomain() }) }
                        .catch { e -> emit(Result.failure(e)) },
                )
            }

        override suspend fun getMovieById(movieId: Int): Result<Movie?> =
            try {
                val movie = movieDao.getMovieById(movieId)?.toDomain()
                Result.success(movie)
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override fun searchWatchedMoviesByTitle(query: String): Flow<Result<List<Movie>>> =
            flow {
                emitAll(
                    movieDao
                        .searchWatchedMoviesByTitle(query)
                        .map { entities ->
                            Result.success(entities.map { it.toDomain() })
                        }.catch { e -> emit(Result.failure(e)) },
                )
            }

        override fun searchWatchlistMoviesByTitle(query: String): Flow<Result<List<Movie>>> =
            flow {
                emitAll(
                    movieDao
                        .searchWatchlistMoviesByTitle(query)
                        .map { entities ->
                            Result.success(entities.map { it.toDomain() })
                        }.catch { e -> emit(Result.failure(e)) },
                )
            }

        override suspend fun updateMovieState(movie: Movie): Result<Boolean> =
            try {
                movieDao.insertOrUpdateMovie(movie.toEntity())
                Result.success(true)
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun removeFromWatchlist(movieId: Int): Result<Boolean> =
            try {
                movieDao.removeFromWatchlist(movieId)
                Result.success(true)
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun getMoviesForSync(): Result<List<Movie>> =
            try {
                Result.success(movieDao.getMoviesForSync().map { it.toDomain() })
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
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
                Result.success(Unit)
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
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
                Result.success(Unit)
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun clearAllUserData(): Result<Unit> =
            try {
                movieDao.clearAllUserData()
                Result.success(Unit)
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
