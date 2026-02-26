package com.asensiodev.feature.watchlist.impl.presentation

import com.asensiodev.core.domain.Result
import com.asensiodev.core.testing.coVerifyOnce
import com.asensiodev.feature.watchlist.impl.domain.usecase.GetWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.RemoveFromWatchlistUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.SearchWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistMoviesViewModelTest {
    private val getWatchlistMoviesUseCase: GetWatchlistMoviesUseCase = mockk(relaxed = true)
    private val searchWatchlistMoviesUseCase: SearchWatchlistMoviesUseCase = mockk(relaxed = true)
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase = mockk(relaxed = true)
    private val syncScheduler: WorkManagerSyncScheduler = mockk(relaxed = true)

    private lateinit var viewModel: WatchlistMoviesViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val inceptionMovieUi =
        MovieUi(
            id = 1,
            title = "Inception",
            posterPath = "https://image.tmdb.org/t/p/w500/inception.jpg",
            releaseYear = "2010",
            genres = "",
            rating = 8.8,
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getWatchlistMoviesUseCase() } returns flowOf(Result.Success(emptyList()))
        viewModel =
            WatchlistMoviesViewModel(
                getWatchlistMoviesUseCase = getWatchlistMoviesUseCase,
                searchWatchlistMoviesUseCase = searchWatchlistMoviesUseCase,
                removeFromWatchlistUseCase = removeFromWatchlistUseCase,
                syncScheduler = syncScheduler,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a movie WHEN RequestRemove intent THEN movieToRemove is set in state`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.RequestRemove(inceptionMovieUi))

            viewModel.uiState.value.movieToRemove shouldBeEqualTo inceptionMovieUi
        }

    @Test
    fun `GIVEN movieToRemove set WHEN DismissRemoveDialog intent THEN movieToRemove is cleared`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.RequestRemove(inceptionMovieUi))
            viewModel.process(WatchlistIntent.DismissRemoveDialog)

            viewModel.uiState.value.movieToRemove
                .shouldBeNull()
        }

    @Test
    fun `GIVEN movieToRemove set WHEN ConfirmRemove intent THEN calls use case and clears movieToRemove`() =
        runTest {
            coEvery { removeFromWatchlistUseCase(inceptionMovieUi.id) } returns Result.Success(true)
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.RequestRemove(inceptionMovieUi))
            viewModel.process(WatchlistIntent.ConfirmRemove)
            advanceUntilIdle()

            viewModel.uiState.value.movieToRemove
                .shouldBeNull()
            coVerifyOnce { removeFromWatchlistUseCase(inceptionMovieUi.id) }
        }

    @Test
    fun `GIVEN no movieToRemove WHEN ConfirmRemove intent THEN use case is never called`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.ConfirmRemove)
            advanceUntilIdle()

            viewModel.uiState.value.movieToRemove
                .shouldBeNull()
        }

    @Test
    fun `GIVEN movieToRemove set WHEN ConfirmRemove intent THEN enqueues upload for that movie`() =
        runTest {
            coEvery { removeFromWatchlistUseCase(inceptionMovieUi.id) } returns Result.Success(true)
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.RequestRemove(inceptionMovieUi))
            viewModel.process(WatchlistIntent.ConfirmRemove)
            advanceUntilIdle()

            coVerify(exactly = 1) { syncScheduler.enqueueUpload(inceptionMovieUi.id) }
        }

    @Test
    fun `GIVEN remove fails WHEN ConfirmRemove intent THEN does not enqueue upload`() =
        runTest {
            coEvery {
                removeFromWatchlistUseCase(inceptionMovieUi.id)
            } returns Result.Error(Exception("db error"))
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.RequestRemove(inceptionMovieUi))
            viewModel.process(WatchlistIntent.ConfirmRemove)
            advanceUntilIdle()

            coVerify(exactly = 0) { syncScheduler.enqueueUpload(any()) }
        }

    @Test
    fun `GIVEN no movieToRemove WHEN ConfirmRemove intent THEN never enqueues upload`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.ConfirmRemove)
            advanceUntilIdle()

            coVerify(exactly = 0) { syncScheduler.enqueueUpload(any()) }
        }
}
