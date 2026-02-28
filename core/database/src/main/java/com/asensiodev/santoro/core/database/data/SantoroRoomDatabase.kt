package com.asensiodev.santoro.core.database.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.asensiodev.santoro.core.database.data.dao.BrowseCacheDao
import com.asensiodev.santoro.core.database.data.dao.MovieDao
import com.asensiodev.santoro.core.database.data.model.BrowseCacheEntity
import com.asensiodev.santoro.core.database.data.model.MovieEntity

@Database(
    entities = [MovieEntity::class, BrowseCacheEntity::class],
    version = 5,
    exportSchema = true,
)
abstract class SantoroRoomDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun browseCacheDao(): BrowseCacheDao

    companion object {
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE movies ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS browse_cache (" +
                            "section TEXT NOT NULL, " +
                            "page INTEGER NOT NULL, " +
                            "moviesJson TEXT NOT NULL, " +
                            "cachedAt INTEGER NOT NULL, " +
                            "PRIMARY KEY(section, page)" +
                            ")",
                    )
                }
            }

        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE movies ADD COLUMN tagline TEXT")
                }
            }

        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE movies ADD COLUMN runtime INTEGER")
                }
            }
    }
}
