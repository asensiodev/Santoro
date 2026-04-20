package com.asensiodev.santoro.core.sync.data.repository

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import com.asensiodev.santoro.core.sync.data.datasource.FirestoreMovieDataSource
import com.asensiodev.santoro.core.sync.data.model.MovieSyncEntity
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import com.google.gson.Gson
import javax.inject.Inject

internal class DefaultSyncRepository
    @Inject
    constructor(
        private val firestoreDataSource: FirestoreMovieDataSource,
        private val databaseRepository: DatabaseRepository,
    ) : SyncRepository {
        private val gson = Gson()

        override suspend fun uploadPendingChanges(uid: String): Result<Unit> {
            val moviesResult = databaseRepository.getMoviesForSync()
            if (moviesResult.isFailure) {
                return Result.failure(
                    moviesResult.exceptionOrNull() ?: Exception("Failed to get movies for sync"),
                )
            }

            return uploadMovies(uid, moviesResult.getOrThrow())
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
                        genres = gson.toJson(movie.genres),
                        runtime = movie.runtime,
                        isWatched = movie.isWatched,
                        isInWatchlist = movie.isInWatchlist,
                        watchedAt = movie.watchedAt,
                        updatedAt = movie.updatedAt,
                    )
                val uploadResult = firestoreDataSource.uploadMovie(uid, entity)
                if (uploadResult.isFailure) {
                    return Result.failure(
                        uploadResult.exceptionOrNull() ?: Exception("Upload failed"),
                    )
                }
            }
            return Result.success(Unit)
        }

        override suspend fun downloadAndMerge(uid: String): Result<Unit> {
            val downloadResult = firestoreDataSource.downloadUserMovies(uid)
            if (downloadResult.isFailure) {
                return Result.failure(
                    downloadResult.exceptionOrNull() ?: Exception("Download failed"),
                )
            }
            return mergeWithLocalMovies(downloadResult.getOrDefault(emptyList()))
        }

        private suspend fun mergeWithLocalMovies(
            remoteMovies: List<MovieSyncEntity>,
        ): Result<Unit> {
            val localMoviesResult = databaseRepository.getMoviesForSync()
            if (localMoviesResult.isFailure) {
                return Result.failure(
                    localMoviesResult.exceptionOrNull() ?: Exception("Failed to get local movies"),
                )
            }
            return mergeMovies(
                remoteMovies = remoteMovies,
                localMoviesById = localMoviesResult.getOrThrow().associateBy { it.id },
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
                        localMovie != null && remote.updatedAt > localMovie.updatedAt -> {
                            databaseRepository.updateMovieSyncState(
                                movieId = remote.movieId,
                                isWatched = remote.isWatched,
                                isInWatchlist = remote.isInWatchlist,
                                watchedAt = remote.watchedAt,
                                updatedAt = remote.updatedAt,
                            )
                        }

                        localMovie != null -> {
                            Result.success(Unit)
                        }

                        else -> {
                            mergeMissingFromSyncLocalMovie(remote)
                        }
                    }
                if (result.isFailure) {
                    return Result.failure(
                        result.exceptionOrNull() ?: Exception("Merge failed"),
                    )
                }
            }
            return Result.success(Unit)
        }

        private suspend fun mergeMissingFromSyncLocalMovie(remote: MovieSyncEntity): Result<Unit> {
            val savedLocallyResult = databaseRepository.getMovieById(remote.movieId)
            if (savedLocallyResult.isFailure) {
                return Result.failure(
                    savedLocallyResult.exceptionOrNull() ?: Exception("Failed to get local movie"),
                )
            }

            val savedLocally = savedLocallyResult.getOrNull()
            return when {
                savedLocally == null ->
                    databaseRepository.upsertMovieFromSync(
                        movieId = remote.movieId,
                        title = remote.title,
                        posterPath = remote.posterPath,
                        genres = remote.genres,
                        runtime = remote.runtime,
                        isWatched = remote.isWatched,
                        isInWatchlist = remote.isInWatchlist,
                        watchedAt = remote.watchedAt,
                        updatedAt = remote.updatedAt,
                    )

                remote.updatedAt > savedLocally.updatedAt ->
                    databaseRepository.updateMovieSyncState(
                        movieId = remote.movieId,
                        isWatched = remote.isWatched,
                        isInWatchlist = remote.isInWatchlist,
                        watchedAt = remote.watchedAt,
                        updatedAt = remote.updatedAt,
                    )

                else -> Result.success(Unit)
            }
        }
    }
