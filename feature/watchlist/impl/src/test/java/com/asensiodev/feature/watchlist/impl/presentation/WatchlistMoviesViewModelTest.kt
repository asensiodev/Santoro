package com.asensiodev.feature.watchlist.impl.presentation

import com.asensiodev.core.domain.Result
import com.asensiodev.core.testing.coVerifyOnce
import com.asensiodev.feature.watchlist.impl.domain.usecase.GetWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.RemoveFromWatchlistUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.SearchWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import io.mockk.coEvery
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
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a movie WHEN onRemoveMovieClicked THEN movieToRemove is set in state`() =
        runTest {
            advanceUntilIdle()

            viewModel.onRemoveMovieClicked(inceptionMovieUi)

            viewModel.uiState.value.movieToRemove shouldBeEqualTo inceptionMovieUi
        }

    @Test
    fun `GIVEN movieToRemove set WHEN onRemoveDismissed THEN movieToRemove is cleared`() =
        runTest {
            advanceUntilIdle()

            viewModel.onRemoveMovieClicked(inceptionMovieUi)
            viewModel.onRemoveDismissed()

            viewModel.uiState.value.movieToRemove
                .shouldBeNull()
        }

    @Test
    fun `GIVEN movieToRemove set WHEN onRemoveConfirmed THEN calls use case and clears movieToRemove`() =
        runTest {
            coEvery { removeFromWatchlistUseCase(inceptionMovieUi.id) } returns Result.Success(true)
            advanceUntilIdle()

            viewModel.onRemoveMovieClicked(inceptionMovieUi)
            viewModel.onRemoveConfirmed()
            advanceUntilIdle()

            viewModel.uiState.value.movieToRemove
                .shouldBeNull()
            coVerifyOnce { removeFromWatchlistUseCase(inceptionMovieUi.id) }
        }

    @Test
    fun `GIVEN no movieToRemove WHEN onRemoveConfirmed THEN use case is never called`() =
        runTest {
            advanceUntilIdle()

            viewModel.onRemoveConfirmed()
            advanceUntilIdle()

            viewModel.uiState.value.movieToRemove
                .shouldBeNull()
        }
}
