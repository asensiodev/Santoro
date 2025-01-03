package com.asensiodev.santoro.core.database.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.asensiodev.santoro.core.database.data.model.MovieEntity

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies WHERE isInWatchlist = 1")
    suspend fun getWatchlistMovies(): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE isWatched = 1")
    suspend fun getWatchedMovies(): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMovie(movie: MovieEntity)

    @Update
    suspend fun updateMovie(movie: MovieEntity)
}
