package com.asensiodev.santoro.core.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.asensiodev.santoro.core.database.data.model.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("TooManyFunctions")
interface MovieDao {
    @Query("SELECT * FROM movies WHERE isWatched = 1 ORDER BY watchedAt DESC")
    fun getWatchedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isInWatchlist = 1")
    fun getWatchlistMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Query(
        """SELECT * FROM movies
           WHERE isWatched = 1
           AND LOWER(title) LIKE '%' || LOWER(:query) || '%'
           ORDER BY watchedAt DESC""",
    )
    fun searchWatchedMoviesByTitle(query: String): Flow<List<MovieEntity>>

    @Query(
        """SELECT * FROM movies
           WHERE isInWatchlist = 1
           AND LOWER(title) LIKE '%' || LOWER(:query) || '%'""",
    )
    fun searchWatchlistMoviesByTitle(query: String): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMovie(movie: MovieEntity)

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Query("UPDATE movies SET isInWatchlist = 0, updatedAt = :updatedAt WHERE id = :movieId")
    suspend fun removeFromWatchlist(
        movieId: Int,
        updatedAt: Long,
    )

    @Query("SELECT * FROM movies WHERE updatedAt > 0")
    suspend fun getMoviesForSync(): List<MovieEntity>

    @Query(
        """UPDATE movies
           SET isWatched = :isWatched,
               isInWatchlist = :isInWatchlist,
               watchedAt = :watchedAt,
               updatedAt = :updatedAt
           WHERE id = :movieId""",
    )
    suspend fun updateMovieSyncState(
        movieId: Int,
        isWatched: Boolean,
        isInWatchlist: Boolean,
        watchedAt: Long?,
        updatedAt: Long,
    )

    @Query("DELETE FROM movies")
    suspend fun clearMovies()
}
