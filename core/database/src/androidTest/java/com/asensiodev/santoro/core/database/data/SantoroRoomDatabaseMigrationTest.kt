package com.asensiodev.santoro.core.database.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SantoroRoomDatabaseMigrationTest {
    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            SantoroRoomDatabase::class.java,
        )

    @Before
    fun setUp() {
        deleteDatabase()
    }

    @After
    fun tearDown() {
        deleteDatabase()
    }

    @Test
    fun givenVersion2_whenMigratedTo3_thenMoviePreservedAndBrowseCacheCreated() {
        helper.createDatabase(DATABASE_NAME, 2).use { database -> insertMovie(database, 2) }

        val migrated =
            helper.runMigrationsAndValidate(
                DATABASE_NAME,
                3,
                true,
                SantoroRoomDatabase.MIGRATION_2_3,
            )

        migrated.use { database ->
            assertMovie(database, expectedTagline = null, expectedRuntime = null)
            insertAndAssertCache(database)
        }
    }

    @Test
    fun givenVersion3_whenMigratedTo4_thenDataPreservedAndTaglineDefaultsToNull() {
        helper.createDatabase(DATABASE_NAME, 3).use { database ->
            insertMovie(database, 3)
            insertCache(database)
        }

        val migrated =
            helper.runMigrationsAndValidate(
                DATABASE_NAME,
                4,
                true,
                SantoroRoomDatabase.MIGRATION_3_4,
            )

        migrated.use { database ->
            assertMovie(database, expectedTagline = null, expectedRuntime = null)
            assertCache(database)
        }
    }

    @Test
    fun givenVersion4_whenMigratedTo5_thenDataPreservedAndRuntimeDefaultsToNull() {
        helper.createDatabase(DATABASE_NAME, 4).use { database ->
            insertMovie(database, 4, tagline = "Synthetic tagline")
            insertCache(database)
        }

        val migrated =
            helper.runMigrationsAndValidate(
                DATABASE_NAME,
                5,
                true,
                SantoroRoomDatabase.MIGRATION_4_5,
            )

        migrated.use { database ->
            assertMovie(database, expectedTagline = "Synthetic tagline", expectedRuntime = null)
            assertCache(database)
        }
    }

    @Test
    fun givenOldestAvailableVersion_whenMigratedTo5_thenCompletePathPreservesDataAndDefaults() {
        helper.createDatabase(DATABASE_NAME, 2).use { database -> insertMovie(database, 2) }

        val migrated =
            helper.runMigrationsAndValidate(
                DATABASE_NAME,
                5,
                true,
                SantoroRoomDatabase.MIGRATION_2_3,
                SantoroRoomDatabase.MIGRATION_3_4,
                SantoroRoomDatabase.MIGRATION_4_5,
            )

        migrated.use { database ->
            assertMovie(database, expectedTagline = null, expectedRuntime = null)
            insertAndAssertCache(database)
        }
    }

    private fun insertMovie(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        version: Int,
        tagline: String? = null,
    ) {
        val columns =
            buildList {
                addAll(BASE_MOVIE_COLUMNS)
                if (version >= 4) add("tagline")
            }
        val values =
            buildList<Any?> {
                addAll(BASE_MOVIE_VALUES)
                if (version >= 4) add(tagline)
            }.toTypedArray()
        val placeholders = columns.joinToString(",") { "?" }
        database.execSQL(
            "INSERT INTO movies (${columns.joinToString(",")}) VALUES ($placeholders)",
            values,
        )
    }

    private fun assertMovie(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        expectedTagline: String?,
        expectedRuntime: Int?,
    ) {
        database.query("SELECT * FROM movies WHERE id = $MOVIE_ID").use { cursor ->
            cursor.moveToFirst().shouldBeTrue()
            cursor.getString(cursor.getColumnIndexOrThrow("title")) shouldBeEqualTo MOVIE_TITLE
            cursor.getString(cursor.getColumnIndexOrThrow("overview")) shouldBeEqualTo "Overview"
            cursor.getString(
                cursor.getColumnIndexOrThrow("posterPath"),
            ) shouldBeEqualTo "/poster.jpg"
            cursor.getString(
                cursor.getColumnIndexOrThrow("releaseDate"),
            ) shouldBeEqualTo "2026-01-01"
            cursor.getDouble(cursor.getColumnIndexOrThrow("popularity")) shouldBeEqualTo 8.5
            cursor.getDouble(cursor.getColumnIndexOrThrow("voteAverage")) shouldBeEqualTo 7.5
            cursor.getInt(cursor.getColumnIndexOrThrow("voteCount")) shouldBeEqualTo 100
            cursor.getString(cursor.getColumnIndexOrThrow("genres")) shouldBeEqualTo "[\"Drama\"]"
            cursor.getString(
                cursor.getColumnIndexOrThrow("productionCountries"),
            ) shouldBeEqualTo "[\"ES\"]"
            cursor.getInt(cursor.getColumnIndexOrThrow("isWatched")) shouldBeEqualTo 1
            cursor.getInt(cursor.getColumnIndexOrThrow("isInWatchlist")) shouldBeEqualTo 1
            cursor.getLong(cursor.getColumnIndexOrThrow("watchedAt")) shouldBeEqualTo 300L
            cursor.getLong(cursor.getColumnIndexOrThrow("updatedAt")) shouldBeEqualTo UPDATED_AT
            val taglineIndex = cursor.getColumnIndex("tagline")
            if (taglineIndex >= 0) cursor.getString(taglineIndex) shouldBeEqualTo expectedTagline
            val runtimeIndex = cursor.getColumnIndex("runtime")
            if (runtimeIndex >= 0) {
                val runtime = if (cursor.isNull(runtimeIndex)) null else cursor.getInt(runtimeIndex)
                runtime shouldBeEqualTo expectedRuntime
            }
        }
    }

    private fun insertAndAssertCache(database: androidx.sqlite.db.SupportSQLiteDatabase) {
        insertCache(database)
        assertCache(database)
    }

    private fun insertCache(database: androidx.sqlite.db.SupportSQLiteDatabase) {
        database.execSQL(
            "INSERT INTO browse_cache (section,page,moviesJson,cachedAt) VALUES (?,?,?,?)",
            arrayOf<Any>("popular", 1, "[]", 500L),
        )
    }

    private fun assertCache(database: androidx.sqlite.db.SupportSQLiteDatabase) {
        val query = "SELECT * FROM browse_cache WHERE section = 'popular' AND page = 1"
        database.query(query).use { cursor ->
            cursor.moveToFirst().shouldBeTrue()
            cursor.getString(cursor.getColumnIndexOrThrow("moviesJson")) shouldBeEqualTo "[]"
            cursor.getLong(cursor.getColumnIndexOrThrow("cachedAt")) shouldBeEqualTo 500L
        }
    }

    private fun deleteDatabase() {
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(DATABASE_NAME)
    }

    private companion object {
        const val DATABASE_NAME = "fip-019-migration-test"
        const val MOVIE_ID = 42
        const val MOVIE_TITLE = "Synthetic Movie"
        const val UPDATED_AT = 400L
        val BASE_MOVIE_COLUMNS =
            listOf(
                "id",
                "title",
                "overview",
                "posterPath",
                "releaseDate",
                "popularity",
                "voteAverage",
                "voteCount",
                "genres",
                "productionCountries",
                "isWatched",
                "isInWatchlist",
                "watchedAt",
                "updatedAt",
            )
        val BASE_MOVIE_VALUES =
            listOf(
                MOVIE_ID,
                MOVIE_TITLE,
                "Overview",
                "/poster.jpg",
                "2026-01-01",
                8.5,
                7.5,
                100,
                "[\"Drama\"]",
                "[\"ES\"]",
                1,
                1,
                300L,
                UPDATED_AT,
            )
    }
}
