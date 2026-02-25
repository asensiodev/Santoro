package com.asensiodev.santoro.core.sync.data.repository

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import com.asensiodev.santoro.core.sync.data.datasource.FirestoreMovieDataSource
import com.asensiodev.santoro.core.sync.data.model.MovieSyncEntity
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import javax.inject.Inject

internal class DefaultSyncRepository
    @Inject
    constructor(
        private val firestoreDataSource: FirestoreMovieDataSource,
        private val databaseRepository: DatabaseRepository,
    ) : SyncRepository {
        override suspend fun uploadPendingChanges(uid: String): Result<Unit> {
            val moviesResult = databaseRepository.getMoviesForSync()
            if (moviesResult is Result.Error) return Result.Error(moviesResult.exception)

            return uploadMovies(uid, (moviesResult as Result.Success).data)
        }

        private suspend fun uploadMovies(
            uid: String,
            movies: List<Movie>,
        ): Result<Unit> {
            movies.forEach { movie ->
                val entity =
                    MovieSyncEntity(
                        movieId = movie.id,
                        title = movie.title,
                        posterPath = movie.posterPath,
                        isWatched = movie.isWatched,
                        isInWatchlist = movie.isInWatchlist,
                        watchedAt = movie.watchedAt,
                        updatedAt = movie.updatedAt,
                    )
                val uploadResult = firestoreDataSource.uploadMovie(uid, entity)
                if (uploadResult.isFailure) {
                    return Result.Error(
                        uploadResult.exceptionOrNull() ?: Exception("Upload failed"),
                    )
                }
            }
            return Result.Success(Unit)
        }

        override suspend fun downloadAndMerge(uid: String): Result<Unit> {
            val downloadResult = firestoreDataSource.downloadUserMovies(uid)
            if (downloadResult.isFailure) {
                return Result.Error(
                    downloadResult.exceptionOrNull() ?: Exception("Download failed"),
                )
            }
            return mergeWithLocalMovies(downloadResult.getOrDefault(emptyList()))
        }

        private suspend fun mergeWithLocalMovies(
            remoteMovies: List<MovieSyncEntity>,
        ): Result<Unit> {
            val localMoviesResult = databaseRepository.getMoviesForSync()
            if (localMoviesResult is Result.Error) return Result.Error(localMoviesResult.exception)
            return mergeMovies(
                remoteMovies = remoteMovies,
                localMoviesById = (localMoviesResult as Result.Success).data.associateBy { it.id },
            )
        }

        private suspend fun mergeMovies(
            remoteMovies: List<MovieSyncEntity>,
            localMoviesById: Map<Int, Movie>,
        ): Result<Unit> {
            remoteMovies.forEach { remote ->
                val localMovie = localMoviesById[remote.movieId]
                val result =
                    when {
                        localMovie == null -> {
                            databaseRepository.upsertMovieFromSync(
                                movieId = remote.movieId,
                                title = remote.title,
                                posterPath = remote.posterPath,
                                isWatched = remote.isWatched,
                                isInWatchlist = remote.isInWatchlist,
                                watchedAt = remote.watchedAt,
                                updatedAt = remote.updatedAt,
                            )
                        }

                        remote.updatedAt > localMovie.updatedAt -> {
                            databaseRepository.updateMovieSyncState(
                                movieId = remote.movieId,
                                isWatched = remote.isWatched,
                                isInWatchlist = remote.isInWatchlist,
                                watchedAt = remote.watchedAt,
                                updatedAt = remote.updatedAt,
                            )
                        }

                        else -> {
                            Result.Success(Unit)
                        }
                    }
                if (result is Result.Error) return Result.Error(result.exception)
            }
            return Result.Success(Unit)
        }
    }
