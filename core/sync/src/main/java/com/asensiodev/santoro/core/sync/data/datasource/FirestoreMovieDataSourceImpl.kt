package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.santoro.core.sync.data.model.MovieSyncEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val COLLECTION_USERS = "users"
private const val COLLECTION_MOVIES = "movies"

private const val FIELD_MOVIE_ID = "movieId"
private const val FIELD_TITLE = "title"
private const val FIELD_POSTER_PATH = "posterPath"
private const val FIELD_GENRES = "genres"
private const val FIELD_RUNTIME = "runtime"
private const val FIELD_IS_WATCHED = "isWatched"
private const val FIELD_IS_IN_WATCHLIST = "isInWatchlist"
private const val FIELD_WATCHED_AT = "watchedAt"
private const val FIELD_UPDATED_AT = "updatedAt"

internal class FirestoreMovieDataSourceImpl
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
    ) : FirestoreMovieDataSource {
        override suspend fun uploadMovie(
            uid: String,
            entity: MovieSyncEntity,
        ): Result<Unit> =
            runCatching {
                val data =
                    mapOf(
                        FIELD_MOVIE_ID to entity.movieId,
                        FIELD_TITLE to entity.title,
                        FIELD_POSTER_PATH to entity.posterPath,
                        FIELD_GENRES to entity.genres,
                        FIELD_RUNTIME to entity.runtime,
                        FIELD_IS_WATCHED to entity.isWatched,
                        FIELD_IS_IN_WATCHLIST to entity.isInWatchlist,
                        FIELD_WATCHED_AT to entity.watchedAt,
                        FIELD_UPDATED_AT to entity.updatedAt,
                    )
                firestore
                    .collection(COLLECTION_USERS)
                    .document(uid)
                    .collection(COLLECTION_MOVIES)
                    .document(entity.movieId.toString())
                    .set(data)
                    .await()
            }

        override suspend fun downloadUserMovies(uid: String): Result<List<MovieSyncEntity>> =
            runCatching {
                firestore
                    .collection(COLLECTION_USERS)
                    .document(uid)
                    .collection(COLLECTION_MOVIES)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        val movieId =
                            (doc.getLong(FIELD_MOVIE_ID) ?: return@mapNotNull null).toInt()
                        val title =
                            doc
                                .getString(FIELD_TITLE)
                                .takeUnless { it.isNullOrEmpty() } ?: return@mapNotNull null
                        MovieSyncEntity(
                            movieId = movieId,
                            title = title,
                            posterPath = doc.getString(FIELD_POSTER_PATH),
                            genres = doc.getString(FIELD_GENRES).orEmpty(),
                            runtime = doc.getLong(FIELD_RUNTIME)?.toInt(),
                            isWatched = doc.getBoolean(FIELD_IS_WATCHED) ?: false,
                            isInWatchlist = doc.getBoolean(FIELD_IS_IN_WATCHLIST) ?: false,
                            watchedAt = doc.getLong(FIELD_WATCHED_AT),
                            updatedAt = doc.getLong(FIELD_UPDATED_AT) ?: 0L,
                        )
                    }
            }
    }
