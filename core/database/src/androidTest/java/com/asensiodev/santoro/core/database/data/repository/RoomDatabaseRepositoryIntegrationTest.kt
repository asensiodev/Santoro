package com.asensiodev.santoro.core.database.data.repository

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.domain.model.MovieSyncData
import com.asensiodev.santoro.core.database.MockUtils
import com.asensiodev.santoro.core.database.data.SantoroRoomDatabase
import com.asensiodev.santoro.core.database.data.model.BrowseCacheEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDatabaseRepositoryIntegrationTest {
    private lateinit var database: SantoroRoomDatabase
    private lateinit var repository: RoomDatabaseRepository

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    SantoroRoomDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        repository = RoomDatabaseRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun clearMovies_removesMoviesAndPreservesBrowseCache(): Unit =
        runBlocking {
            val cache = BrowseCacheEntity("popular", 1, "[]", 10L)
            database.movieDao().insertOrUpdateMovie(MockUtils.createTestMovieEntity(1))
            database.browseCacheDao().upsertPage(cache)

            repository.clearMovies()

            database.movieDao().getMovieById(1) shouldBeEqualTo null
            database.browseCacheDao().getPage("popular", 1) shouldBeEqualTo cache
        }

    @Test
    fun completeDownloadedMerge_appliesOnlyNewerStateAndInsertsMissingMovies(): Unit =
        runBlocking {
            database.movieDao().insertOrUpdateMovie(
                MockUtils.createTestMovieEntity(
                    id = 1,
                    title = "Newer local",
                    isWatched = true,
                    updatedAt = 100L,
                ),
            )
            database.movieDao().insertOrUpdateMovie(
                MockUtils.createTestMovieEntity(2, title = "Rich local", updatedAt = 50L),
            )

            repository
                .completeDownloadedMerge(
                    listOf(
                        syncMovie(1, updatedAt = 90L),
                        syncMovie(2, updatedAt = 110L, isWatched = true),
                        syncMovie(3, updatedAt = 80L, isWatched = true),
                    ),
                    canMerge = { true },
                ).getOrThrow()

            database.movieDao().getMovieById(1)?.isWatched shouldBeEqualTo true
            database.movieDao().getMovieById(1)?.updatedAt shouldBeEqualTo 100L
            database.movieDao().getMovieById(2)?.title shouldBeEqualTo "Rich local"
            database.movieDao().getMovieById(2)?.isWatched shouldBeEqualTo true
            database.movieDao().getMovieById(2)?.updatedAt shouldBeEqualTo 110L
            database.movieDao().getMovieById(3)?.updatedAt shouldBeEqualTo 80L
        }

    @Test
    fun completeDownloadedMerge_rollsBackWhenAWriteFails(): Unit =
        runBlocking {
            database.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER reject_movie BEFORE INSERT ON movies " +
                    "WHEN NEW.id = 2 BEGIN SELECT RAISE(ABORT, 'rejected'); END",
            )

            val result =
                repository.completeDownloadedMerge(
                    listOf(syncMovie(1), syncMovie(2)),
                    canMerge = { true },
                )

            result.isFailure shouldBeEqualTo true
            database.movieDao().getMovieById(1) shouldBeEqualTo null
            database.movieDao().getMovieById(2) shouldBeEqualTo null
        }

    @Test
    fun completeDownloadedMerge_serializesAuthorityCheckAndMergeWithCleanup(): Unit =
        runBlocking {
            val authorityCheckStarted = CompletableDeferred<Unit>()
            val allowMerge = CompletableDeferred<Unit>()
            val merge =
                async {
                    repository.completeDownloadedMerge(listOf(syncMovie(1))) {
                        authorityCheckStarted.complete(Unit)
                        allowMerge.await()
                        true
                    }
                }
            authorityCheckStarted.await()
            val cleanup = async { repository.clearMovies() }

            allowMerge.complete(Unit)
            merge.await().getOrThrow()
            cleanup.await()

            database.movieDao().getMovieById(1) shouldBeEqualTo null
        }

    @Test
    fun clearMovies_rollsBackAndPropagatesCancellation(): Unit =
        runBlocking {
            database.movieDao().insertOrUpdateMovie(MockUtils.createTestMovieEntity(1))

            val exception =
                runCatching {
                    database.withTransaction {
                        repository.clearMovies()
                        throw CancellationException()
                    }
                }.exceptionOrNull()

            exception.shouldBeInstanceOf<CancellationException>()
            database.movieDao().getMovieById(1)?.id shouldBeEqualTo 1
        }

    private fun syncMovie(
        id: Int,
        updatedAt: Long = 1L,
        isWatched: Boolean = false,
    ) = MovieSyncData(
        movieId = id,
        title = "Movie $id",
        posterPath = null,
        genres = emptyList(),
        runtime = null,
        isWatched = isWatched,
        isInWatchlist = false,
        watchedAt = null,
        updatedAt = updatedAt,
    )
}
