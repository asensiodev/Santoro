package com.asensiodev.santoro.core.database.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.asensiodev.santoro.core.database.data.dao.MovieDao
import com.asensiodev.santoro.core.database.data.model.MovieEntity

@Database(entities = [MovieEntity::class], version = 2, exportSchema = true)
abstract class SantoroRoomDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE movies ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                }
            }
    }
}
