package com.asensiodev.santoro.core.database.data.repository

import android.database.sqlite.SQLiteException
import androidx.room.withTransaction
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.MovieSyncData
import com.asensiodev.core.domain.repository.MovieMutationRepository
import com.asensiodev.core.domain.repository.SyncStore
import com.asensiodev.santoro.core.database.data.SantoroRoomDatabase
import com.asensiodev.santoro.core.database.data.dao.MovieDao
import com.asensiodev.santoro.core.database.data.mapper.toDomain
import com.asensiodev.santoro.core.database.data.mapper.toEntity
import com.asensiodev.santoro.core.database.data.mapper.toSyncData
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class RoomDatabaseRepository
    @Inject
    constructor(
        private val database: SantoroRoomDatabase,
    ) : DatabaseRepository,
        MovieMutationRepository,
        SyncStore {
        private val movieDao: MovieDao = database.movieDao()

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

        override suspend fun updateMovieState(movie: Movie): Result<Unit> =
            transactionResult {
                movieDao.insertOrUpdateMovie(movie.toEntity())
            }

        override suspend fun removeFromWatchlist(movieId: Int): Result<Unit> =
            transactionResult {
                movieDao.removeFromWatchlist(movieId, System.currentTimeMillis())
            }

        override suspend fun clearMovies() {
            database.withTransaction { movieDao.clearMovies() }
        }

        override suspend fun getMovieForUpload(movieId: Int): Result<MovieSyncData?> =
            transactionResult {
                movieDao.getMovieById(movieId)?.toSyncData()
            }

        override suspend fun getMoviesForUpload(): Result<List<MovieSyncData>> =
            transactionResult {
                movieDao.getMoviesForSync().map { movie -> movie.toSyncData() }
            }

        override suspend fun completeDownloadedMerge(
            movies: List<MovieSyncData>,
            canMerge: suspend () -> Boolean,
        ): Result<Unit> =
            transactionResult {
                if (!canMerge()) return@transactionResult
                movies.forEach { remote ->
                    val local = movieDao.getMovieById(remote.movieId)
                    when {
                        local == null -> movieDao.insertOrUpdateMovie(remote.toEntity())
                        remote.updatedAt > local.updatedAt ->
                            movieDao.updateMovieSyncState(
                                movieId = remote.movieId,
                                isWatched = remote.isWatched,
                                isInWatchlist = remote.isInWatchlist,
                                watchedAt = remote.watchedAt,
                                updatedAt = remote.updatedAt,
                            )
                    }
                }
            }

        private suspend fun <T> transactionResult(operation: suspend () -> T): Result<T> =
            try {
                Result.success(database.withTransaction { operation() })
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SQLiteException) {
                Result.failure(exception)
            } catch (exception: Exception) {
                Result.failure(exception)
            }
    }
