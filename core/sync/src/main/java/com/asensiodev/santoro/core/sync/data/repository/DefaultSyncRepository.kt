package com.asensiodev.santoro.core.sync.data.repository

import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.MovieSyncData
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.repository.SyncRepository
import com.asensiodev.core.domain.repository.SyncStore
import com.asensiodev.core.domain.result.rethrowCancellation
import com.asensiodev.santoro.core.sync.data.datasource.MovieSyncRemoteDataSource
import com.asensiodev.santoro.core.sync.data.model.MovieSyncEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class DefaultSyncRepository
    @Inject
    constructor(
        private val firestoreDataSource: MovieSyncRemoteDataSource,
        private val syncStore: SyncStore,
        private val authRepository: AuthRepository,
        private val accountDeletionRecoveryRepository: AccountDeletionRecoveryRepository,
    ) : SyncRepository {
        private val gson = Gson()

        override suspend fun uploadMovie(
            uid: String,
            movieId: Int,
        ): Result<Unit> =
            syncStore
                .getMovieForUpload(movieId)
                .rethrowCancellation()
                .fold(
                    onSuccess = { movie -> uploadMovie(uid, movie) },
                    onFailure = Result.Companion::failure,
                )

        override suspend fun uploadLocalSnapshot(uid: String): Result<Unit> =
            syncStore
                .getMoviesForUpload()
                .rethrowCancellation()
                .fold(
                    onSuccess = { movies -> uploadMovies(uid, movies) },
                    onFailure = Result.Companion::failure,
                )

        private suspend fun uploadMovies(
            uid: String,
            movies: List<MovieSyncData>,
        ): Result<Unit> {
            val chunks =
                movies
                    .map { movie -> movie.toSyncEntity() }
                    .chunked(FULL_SNAPSHOT_UPLOAD_CHUNK_SIZE)
                    .iterator()
            var result = Result.success(Unit)
            while (chunks.hasNext() && result.isSuccess && uid.canAccessFirestore()) {
                result = firestoreDataSource.uploadMovies(uid, chunks.next()).rethrowCancellation()
            }
            return result
        }

        private suspend fun uploadMovie(
            uid: String,
            movie: MovieSyncData?,
        ): Result<Unit> {
            val entity = movie?.toSyncEntity() ?: return Result.success(Unit)
            return if (uid.canAccessFirestore()) {
                firestoreDataSource.uploadMovie(uid, entity).rethrowCancellation()
            } else {
                Result.success(Unit)
            }
        }

        override suspend fun downloadAndMerge(uid: String): Result<Unit> {
            if (!uid.canAccessFirestore()) return Result.success(Unit)
            return firestoreDataSource
                .downloadUserMovies(uid)
                .rethrowCancellation()
                .fold(
                    onSuccess = { remoteMovies -> mergeDownloaded(uid, remoteMovies) },
                    onFailure = Result.Companion::failure,
                )
        }

        private suspend fun mergeDownloaded(
            uid: String,
            remoteMovies: List<MovieSyncEntity>,
        ): Result<Unit> =
            try {
                val movies = remoteMovies.map { movie -> movie.toSyncData() }
                if (uid.canAccessFirestore()) {
                    syncStore
                        .completeDownloadedMerge(movies) { uid.canAccessFirestore() }
                        .rethrowCancellation()
                } else {
                    Result.success(Unit)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Result.failure(exception)
            }

        override suspend fun deleteUserData(uid: String): Result<Unit> =
            firestoreDataSource.deleteUserData(uid).rethrowCancellation()

        private fun MovieSyncData.toSyncEntity() =
            MovieSyncEntity(
                movieId = movieId,
                title = title,
                posterPath = posterPath,
                genres = gson.toJson(genres),
                runtime = runtime,
                isWatched = isWatched,
                isInWatchlist = isInWatchlist,
                watchedAt = watchedAt,
                updatedAt = updatedAt,
            )

        private fun MovieSyncEntity.toSyncData(): MovieSyncData {
            val parsedGenres =
                if (genres.isBlank()) {
                    emptyList()
                } else {
                    try {
                        gson.fromJson<List<Genre>?>(genres, GENRES_TYPE).orEmpty()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
            return MovieSyncData(
                movieId = movieId,
                title = title,
                posterPath = posterPath,
                genres = parsedGenres,
                runtime = runtime,
                isWatched = isWatched,
                isInWatchlist = isInWatchlist,
                watchedAt = watchedAt,
                updatedAt = updatedAt,
            )
        }

        private suspend fun String.canAccessFirestore(): Boolean {
            val uid = this
            return combine(
                accountDeletionRecoveryRepository.isLocalCleanupPending,
                accountDeletionRecoveryRepository.isRemoteDeletionInFlight,
                authRepository.currentUser,
            ) { marker, remoteDeletionInFlight, currentUser ->
                !marker && !remoteDeletionInFlight && uid.isNotBlank() && currentUser?.uid == uid
            }.first()
        }

        private companion object {
            const val FULL_SNAPSHOT_UPLOAD_CHUNK_SIZE = 500
            val GENRES_TYPE = object : TypeToken<List<Genre>>() {}.type
        }
    }
