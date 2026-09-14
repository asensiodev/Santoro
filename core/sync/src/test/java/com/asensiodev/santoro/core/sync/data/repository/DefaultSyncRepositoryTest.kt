package com.asensiodev.santoro.core.sync.data.repository

import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.MovieSyncData
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.repository.SyncStore
import com.asensiodev.santoro.core.sync.SyncMockUtils
import com.asensiodev.santoro.core.sync.data.datasource.MovieSyncRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultSyncRepositoryTest {
    private val remoteDataSource: MovieSyncRemoteDataSource = mockk()
    private val syncStore: SyncStore = mockk()
    private val authRepository: AuthRepository = mockk()
    private val deletionRecoveryRepository: AccountDeletionRecoveryRepository = mockk()
    private val currentUser = MutableStateFlow<SantoroUser?>(user(UID))
    private val deletionPending = MutableStateFlow(false)
    private val remoteDeletionInFlight = MutableStateFlow(false)
    private lateinit var sut: DefaultSyncRepository

    @BeforeEach
    fun setUp() {
        every { authRepository.currentUser } returns currentUser
        every { deletionRecoveryRepository.isLocalCleanupPending } returns deletionPending
        every { deletionRecoveryRepository.isRemoteDeletionInFlight } returns remoteDeletionInFlight
        currentUser.value = user(UID)
        deletionPending.value = false
        remoteDeletionInFlight.value = false
        sut =
            DefaultSyncRepository(
                remoteDataSource,
                syncStore,
                authRepository,
                deletionRecoveryRepository,
            )
    }

    @Test
    fun `GIVEN movie snapshot WHEN upload movie THEN maps and uploads it`() =
        runTest {
            coEvery { syncStore.getMovieForUpload(42) } returns
                Result.success(movie(42, listOf(Genre(1, "Drama"))))
            coEvery { remoteDataSource.uploadMovie(any(), any()) } returns Result.success(Unit)

            sut.uploadMovie(UID, 42) shouldBeEqualTo Result.success(Unit)

            coVerify {
                remoteDataSource.uploadMovie(
                    UID,
                    match { it.movieId == 42 && it.genres.contains("Drama") },
                )
            }
        }

    @Test
    fun `GIVEN missing movie WHEN upload movie THEN succeeds without remote access`() =
        runTest {
            coEvery { syncStore.getMovieForUpload(42) } returns Result.success(null)

            sut.uploadMovie(UID, 42) shouldBeEqualTo Result.success(Unit)

            coVerify(exactly = 0) { remoteDataSource.uploadMovie(any(), any()) }
        }

    @Test
    fun `GIVEN auth changes while reading movie WHEN upload starts THEN no remote call starts`() =
        runTest {
            coEvery { syncStore.getMovieForUpload(42) } coAnswers {
                currentUser.value = user("other-uid")
                Result.success(movie(42))
            }

            sut.uploadMovie(UID, 42) shouldBeEqualTo Result.success(Unit)

            coVerify(exactly = 0) { remoteDataSource.uploadMovie(any(), any()) }
        }

    @Test
    fun `GIVEN deletion starts while reading snapshot WHEN upload starts THEN no remote call starts`() =
        runTest {
            coEvery { syncStore.getMoviesForUpload() } coAnswers {
                deletionPending.value = true
                Result.success(listOf(movie(1)))
            }

            sut.uploadLocalSnapshot(UID) shouldBeEqualTo Result.success(Unit)

            coVerify(exactly = 0) { remoteDataSource.uploadMovies(any(), any()) }
        }

    @Test
    fun `GIVEN auth changes after accepted chunk WHEN next chunk starts THEN upload stops`() =
        runTest {
            coEvery { syncStore.getMoviesForUpload() } returns
                Result.success((1..501).map(::movie))
            coEvery { remoteDataSource.uploadMovies(UID, any()) } coAnswers {
                currentUser.value = user("other-uid")
                Result.success(Unit)
            }

            sut.uploadLocalSnapshot(UID) shouldBeEqualTo Result.success(Unit)

            coVerify(exactly = 1) {
                remoteDataSource.uploadMovies(UID, match { entities -> entities.size == 500 })
            }
        }

    @Test
    fun `GIVEN empty snapshot WHEN upload starts THEN succeeds without remote access`() =
        runTest {
            coEvery { syncStore.getMoviesForUpload() } returns Result.success(emptyList())

            sut.uploadLocalSnapshot(UID) shouldBeEqualTo Result.success(Unit)

            coVerify(exactly = 0) { remoteDataSource.uploadMovies(any(), any()) }
        }

    @Test
    fun `GIVEN no auth WHEN download starts THEN succeeds without remote access`() =
        runTest {
            currentUser.value = null

            sut.downloadAndMerge(UID) shouldBeEqualTo Result.success(Unit)

            coVerify(exactly = 0) { remoteDataSource.downloadUserMovies(any()) }
            coVerify(exactly = 0) { syncStore.completeDownloadedMerge(any(), any()) }
        }

    @Test
    fun `GIVEN auth changes during download WHEN response arrives THEN merge does not start`() =
        runTest {
            coEvery { remoteDataSource.downloadUserMovies(UID) } coAnswers {
                currentUser.value = user("other-uid")
                Result.success(listOf(SyncMockUtils.createSyncEntity(9)))
            }

            sut.downloadAndMerge(UID) shouldBeEqualTo Result.success(Unit)

            coVerify(exactly = 0) { syncStore.completeDownloadedMerge(any(), any()) }
        }

    @Test
    fun `GIVEN deletion starts during download WHEN response arrives THEN merge does not start`() =
        runTest {
            coEvery { remoteDataSource.downloadUserMovies(UID) } coAnswers {
                remoteDeletionInFlight.value = true
                Result.success(listOf(SyncMockUtils.createSyncEntity(9)))
            }

            sut.downloadAndMerge(UID) shouldBeEqualTo Result.success(Unit)

            coVerify(exactly = 0) { syncStore.completeDownloadedMerge(any(), any()) }
        }

    @Test
    fun `GIVEN downloaded movies WHEN auth remains current THEN maps and merges them`() =
        runTest {
            val remote = SyncMockUtils.createSyncEntity(9, genres = "[{\"id\":1,\"name\":\"Drama\"}]")
            coEvery { remoteDataSource.downloadUserMovies(UID) } returns Result.success(listOf(remote))
            coEvery { syncStore.completeDownloadedMerge(any(), any()) } returns Result.success(Unit)

            sut.downloadAndMerge(UID) shouldBeEqualTo Result.success(Unit)

            coVerify {
                syncStore.completeDownloadedMerge(
                    match { it.single().movieId == 9 && it.single().genres == listOf(Genre(1, "Drama")) },
                    any(),
                )
            }
        }

    @Test
    fun `GIVEN auth changes before merge transaction WHEN authority is checked THEN merge is rejected`() =
        runTest {
            val remote = SyncMockUtils.createSyncEntity(9)
            var canMerge = true
            coEvery { remoteDataSource.downloadUserMovies(UID) } returns Result.success(listOf(remote))
            coEvery { syncStore.completeDownloadedMerge(any(), any()) } coAnswers {
                currentUser.value = user("other-uid")
                canMerge = secondArg<suspend () -> Boolean>().invoke()
                Result.success(Unit)
            }

            sut.downloadAndMerge(UID) shouldBeEqualTo Result.success(Unit)

            canMerge shouldBeEqualTo false
        }

    @Test
    fun `GIVEN local or remote failure WHEN syncing THEN failure is preserved`() =
        runTest {
            val localFailure = IllegalStateException("local")
            val remoteFailure = IllegalStateException("remote")
            coEvery { syncStore.getMovieForUpload(42) } returns Result.failure(localFailure)
            coEvery { remoteDataSource.downloadUserMovies(UID) } returns Result.failure(remoteFailure)

            sut.uploadMovie(UID, 42).exceptionOrNull() shouldBeEqualTo localFailure
            sut.downloadAndMerge(UID).exceptionOrNull() shouldBeEqualTo remoteFailure
        }

    @Test
    fun `GIVEN cancellation WHEN syncing THEN cancellation propagates`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            coEvery { syncStore.getMoviesForUpload() } returns Result.failure(cancellation)

            runCatching { sut.uploadLocalSnapshot(UID) }.exceptionOrNull() shouldBeEqualTo cancellation

            coEvery { remoteDataSource.downloadUserMovies(UID) } returns Result.failure(cancellation)
            runCatching { sut.downloadAndMerge(UID) }.exceptionOrNull() shouldBeEqualTo cancellation
        }

    @Test
    fun `GIVEN deletion gates active WHEN deleting user data THEN remote deletion remains ungated`() =
        runTest {
            deletionPending.value = true
            remoteDeletionInFlight.value = true
            coEvery { remoteDataSource.deleteUserData(UID) } returns Result.success(Unit)

            sut.deleteUserData(UID) shouldBeEqualTo Result.success(Unit)

            coVerify(exactly = 1) { remoteDataSource.deleteUserData(UID) }
        }

    private fun movie(
        movieId: Int,
        genres: List<Genre> = emptyList(),
    ) = MovieSyncData(
        movieId = movieId,
        title = "Movie $movieId",
        posterPath = null,
        genres = genres,
        runtime = null,
        isWatched = true,
        isInWatchlist = false,
        watchedAt = null,
        updatedAt = 1000L,
    )

    private fun user(uid: String) = SantoroUser(uid, null, null, null, true)

    private companion object {
        const val UID = "uid123"
    }
}
