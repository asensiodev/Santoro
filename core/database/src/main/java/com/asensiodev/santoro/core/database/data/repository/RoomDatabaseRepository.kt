package com.asensiodev.santoro.core.database.data.repository

import android.database.sqlite.SQLiteException
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.santoro.core.database.data.dao.MovieDao
import com.asensiodev.santoro.core.database.data.mapper.toDomain
import com.asensiodev.santoro.core.database.data.mapper.toEntity
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.CancellationException
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
                        .catch { exception ->
                            if (exception is CancellationException) throw exception
                            emit(Result.failure(exception))
                        },
                )
            }

        override fun getWatchlistMovies(): Flow<Result<List<Movie>>> =
            flow {
                emitAll(
                    movieDao
                        .getWatchlistMovies()
                        .map { movies -> Result.success(movies.map { it.toDomain() }) }
                        .catch { exception ->
                            if (exception is CancellationException) throw exception
                            emit(Result.failure(exception))
                        },
                )
            }

        override suspend fun getMovieById(movieId: Int): Result<Movie?> =
            try {
                val movie = movieDao.getMovieById(movieId)?.toDomain()
                Result.success(movie)
            } catch (exception: CancellationException) {
                throw exception
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
                        }.catch { exception ->
                            if (exception is CancellationException) throw exception
                            emit(Result.failure(exception))
                        },
                )
            }

        override fun searchWatchlistMoviesByTitle(query: String): Flow<Result<List<Movie>>> =
            flow {
                emitAll(
                    movieDao
                        .searchWatchlistMoviesByTitle(query)
                        .map { entities ->
                            Result.success(entities.map { it.toDomain() })
                        }.catch { exception ->
                            if (exception is CancellationException) throw exception
                            emit(Result.failure(exception))
                        },
                )
            }

        override suspend fun updateMovieState(movie: Movie): Result<Boolean> =
            try {
                movieDao.insertOrUpdateMovie(movie.toEntity())
                Result.success(true)
            } catch (exception: CancellationException) {
                throw exception
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun removeFromWatchlist(movieId: Int): Result<Boolean> =
            try {
                movieDao.removeFromWatchlist(movieId, System.currentTimeMillis())
                Result.success(true)
            } catch (exception: CancellationException) {
                throw exception
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun getMoviesForSync(): Result<List<Movie>> =
            try {
                Result.success(movieDao.getMoviesForSync().map { it.toDomain() })
            } catch (exception: CancellationException) {
                throw exception
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun upsertMovieFromSync(
            movieId: Int,
            title: String,
            posterPath: String?,
            genres: String,
            runtime: Int?,
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
                    genres,
                    runtime,
                    isWatched,
                    isInWatchlist,
                    watchedAt,
                    updatedAt,
                )
                Result.success(Unit)
            } catch (exception: CancellationException) {
                throw exception
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
            } catch (exception: CancellationException) {
                throw exception
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun clearAllUserData(): Result<Unit> =
            try {
                movieDao.clearAllUserData()
                Result.success(Unit)
            } catch (exception: CancellationException) {
                throw exception
            } catch (e: SQLiteException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
