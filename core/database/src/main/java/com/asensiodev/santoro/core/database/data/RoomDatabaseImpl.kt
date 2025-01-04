package com.asensiodev.santoro.core.database.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.asensiodev.santoro.core.database.data.dao.MovieDao
import com.asensiodev.santoro.core.database.data.model.MovieEntity

@Database(entities = [MovieEntity::class], version = 1, exportSchema = false)
abstract class RoomDatabaseImpl : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
