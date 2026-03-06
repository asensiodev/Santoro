package com.asensiodev.santoro.core.sync.data.repository

import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import com.asensiodev.santoro.core.sync.SyncMockUtils
import com.asensiodev.santoro.core.sync.data.datasource.FirestoreMovieDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultSyncRepositoryTest {
    private val firestoreDataSource: FirestoreMovieDataSource = mockk()
    private val databaseRepository: DatabaseRepository = mockk()

    private lateinit var sut: DefaultSyncRepository

    @BeforeEach
    fun setUp() {
        sut = DefaultSyncRepository(firestoreDataSource, databaseRepository)
    }

    @Test
    fun `GIVEN movies in Room WHEN uploadPendingChanges THEN uploads all to Firestore`() =
        runTest {
            val movies =
                listOf(
                    SyncMockUtils.createMovie(id = 1, isWatched = true),
                    SyncMockUtils.createMovie(id = 2, isInWatchlist = true),
                )
            coEvery { databaseRepository.getMoviesForSync() } returns Result.success(movies)
            coEvery { firestoreDataSource.uploadMovie(any(), any()) } returns Result.success(Unit)

            val result = sut.uploadPendingChanges(uid = "uid123")

            result.isSuccess shouldBeEqualTo true
            coVerify(exactly = 2) { firestoreDataSource.uploadMovie(any(), any()) }
        }

    @Test
    fun `GIVEN getMoviesForSync fails WHEN uploadPendingChanges THEN returns error without uploading`() =
        runTest {
            coEvery { databaseRepository.getMoviesForSync() } returns Result.failure(Exception("db error"))

            val result = sut.uploadPendingChanges(uid = "uid123")

            result.isFailure shouldBeEqualTo true
            coVerify(exactly = 0) { firestoreDataSource.uploadMovie(any(), any()) }
        }

    @Test
    fun `GIVEN no movies in Room WHEN uploadPendingChanges THEN uploads nothing and returns success`() =
        runTest {
            coEvery { databaseRepository.getMoviesForSync() } returns Result.success(emptyList())

            val result = sut.uploadPendingChanges(uid = "uid123")

            result.isSuccess shouldBeEqualTo true
            coVerify(exactly = 0) { firestoreDataSource.uploadMovie(any(), any()) }
        }

    @Test
    fun `GIVEN movie WHEN uploadPendingChanges THEN uploads with correct movieId`() =
        runTest {
            val movie = SyncMockUtils.createMovie(id = 42, isWatched = true)
            coEvery { databaseRepository.getMoviesForSync() } returns Result.success(listOf(movie))
            coEvery { firestoreDataSource.uploadMovie(any(), any()) } returns Result.success(Unit)

            sut.uploadPendingChanges(uid = "uid123")

            coVerify(exactly = 1) {
                firestoreDataSource.uploadMovie("uid123", match { it.movieId == 42 })
            }
        }

    @Test
    fun `GIVEN upload fails mid-batch WHEN uploadPendingChanges THEN returns error early`() =
        runTest {
            val movies =
                listOf(
                    SyncMockUtils.createMovie(id = 1, isWatched = true),
                    SyncMockUtils.createMovie(id = 2, isWatched = true),
                )
            coEvery { databaseRepository.getMoviesForSync() } returns Result.success(movies)
            coEvery {
                firestoreDataSource.uploadMovie("uid123", match { it.movieId == 1 })
            } returns Result.failure(Exception("network"))
            coEvery {
                firestoreDataSource.uploadMovie("uid123", match { it.movieId == 2 })
            } returns Result.success(Unit)

            val result = sut.uploadPendingChanges(uid = "uid123")

            result.isFailure shouldBeEqualTo true
            coVerify(exactly = 0) {
                firestoreDataSource.uploadMovie("uid123", match { it.movieId == 2 })
            }
        }

    @Test
    fun `GIVEN Firestore newer WHEN downloadAndMerge THEN upserts into Room`() =
        runTest {
            val remoteEntity = SyncMockUtils.createSyncEntity(movieId = 1, updatedAt = 2000L)
            val localMovie = SyncMockUtils.createMovie(id = 1, updatedAt = 1000L)

            coEvery {
                firestoreDataSource.downloadUserMovies(any())
            } returns Result.success(listOf(remoteEntity))
            coEvery { databaseRepository.getMoviesForSync() } returns Result.success(listOf(localMovie))
            coEvery {
                databaseRepository.updateMovieSyncState(any(), any(), any(), any(), any())
            } returns Result.success(Unit)

            val result = sut.downloadAndMerge(uid = "uid123")

            result.isSuccess shouldBeEqualTo true
            coVerify(exactly = 1) {
                databaseRepository.updateMovieSyncState(1, any(), any(), any(), 2000L)
            }
        }

    @Test
    fun `GIVEN Room newer WHEN downloadAndMerge THEN does NOT upsert`() =
        runTest {
            val remoteEntity = SyncMockUtils.createSyncEntity(movieId = 1, updatedAt = 500L)
            val localMovie = SyncMockUtils.createMovie(id = 1, updatedAt = 1000L)

            coEvery {
                firestoreDataSource.downloadUserMovies(any())
            } returns Result.success(listOf(remoteEntity))
            coEvery { databaseRepository.getMoviesForSync() } returns Result.success(listOf(localMovie))

            val result = sut.downloadAndMerge(uid = "uid123")

            result.isSuccess shouldBeEqualTo true
            coVerify(exactly = 0) {
                databaseRepository.updateMovieSyncState(any(), any(), any(), any(), any())
            }
        }

    @Test
    fun `GIVEN movie not in Room WHEN downloadAndMerge THEN upserts it from Firestore`() =
        runTest {
            val remoteEntity = SyncMockUtils.createSyncEntity(movieId = 99, updatedAt = 5000L)

            coEvery {
                firestoreDataSource.downloadUserMovies(any())
            } returns Result.success(listOf(remoteEntity))
            coEvery { databaseRepository.getMoviesForSync() } returns Result.success(emptyList())
            coEvery {
                databaseRepository.upsertMovieFromSync(any(), any(), any(), any(), any(), any(), any())
            } returns Result.success(Unit)

            val result = sut.downloadAndMerge(uid = "uid123")

            result.isSuccess shouldBeEqualTo true
            coVerify(exactly = 1) {
                databaseRepository.upsertMovieFromSync(99, "Test Movie", null, false, false, null, 5000L)
            }
            coVerify(exactly = 0) {
                databaseRepository.updateMovieSyncState(any(), any(), any(), any(), any())
            }
        }

    @Test
    fun `GIVEN Firestore download fails WHEN downloadAndMerge THEN returns error`() =
        runTest {
            coEvery {
                firestoreDataSource.downloadUserMovies(any())
            } returns Result.failure(Exception("network error"))

            val result = sut.downloadAndMerge(uid = "uid123")

            result.isFailure shouldBeEqualTo true
        }

    @Test
    fun `GIVEN getMoviesForSync fails WHEN downloadAndMerge THEN returns error`() =
        runTest {
            coEvery {
                firestoreDataSource.downloadUserMovies(any())
            } returns Result.success(emptyList())
            coEvery { databaseRepository.getMoviesForSync() } returns Result.failure(Exception("db error"))

            val result = sut.downloadAndMerge(uid = "uid123")

            result.isFailure shouldBeEqualTo true
        }

    @Test
    fun `GIVEN upsert fails WHEN downloadAndMerge THEN returns error`() =
        runTest {
            val remoteEntity = SyncMockUtils.createSyncEntity(movieId = 1, updatedAt = 2000L)
            val localMovie = SyncMockUtils.createMovie(id = 1, updatedAt = 1000L)

            coEvery {
                firestoreDataSource.downloadUserMovies(any())
            } returns Result.success(listOf(remoteEntity))
            coEvery { databaseRepository.getMoviesForSync() } returns Result.success(listOf(localMovie))
            coEvery {
                databaseRepository.updateMovieSyncState(any(), any(), any(), any(), any())
            } returns Result.failure(Exception("db error"))

            val result = sut.downloadAndMerge(uid = "uid123")

            result.isFailure shouldBeEqualTo true
        }
}
