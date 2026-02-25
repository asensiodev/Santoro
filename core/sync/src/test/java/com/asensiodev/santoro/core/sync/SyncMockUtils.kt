package com.asensiodev.santoro.core.sync

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.santoro.core.sync.data.model.MovieSyncEntity

internal object SyncMockUtils {
    fun createMovie(
        id: Int = 1,
        title: String = "Test Movie",
        isWatched: Boolean = false,
        isInWatchlist: Boolean = false,
        watchedAt: Long? = null,
        updatedAt: Long = 1000L,
    ) = Movie(
        id = id,
        title = title,
        overview = "Overview",
        posterPath = null,
        backdropPath = null,
        releaseDate = "2023-01-01",
        popularity = 7.5,
        voteAverage = 8.0,
        voteCount = 100,
        genres = emptyList(),
        productionCountries = emptyList(),
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
        updatedAt = updatedAt,
    )

    fun createSyncEntity(
        movieId: Int = 1,
        title: String = "Test Movie",
        isWatched: Boolean = false,
        isInWatchlist: Boolean = false,
        watchedAt: Long? = null,
        updatedAt: Long = 1000L,
    ) = MovieSyncEntity(
        movieId = movieId,
        title = title,
        posterPath = null,
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
        updatedAt = updatedAt,
    )

    fun <T> successOf(data: T) = Result.Success(data)
    fun errorOf(e: Exception = Exception("error")) = Result.Error(e)
}
