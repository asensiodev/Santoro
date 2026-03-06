package com.asensiodev.feature.moviedetail.impl.presentation

import app.cash.turbine.test
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.extension.CoroutineTestExtension
import com.asensiodev.feature.moviedetail.impl.domain.usecase.GetMovieDetailUseCase
import com.asensiodev.feature.moviedetail.impl.domain.usecase.UpdateMovieStateUseCase
import com.asensiodev.feature.moviedetail.impl.presentation.mapper.toUi
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {
    @RegisterExtension
    val coroutineTestExtension = CoroutineTestExtension()

    private val getMovieDetailUseCase: GetMovieDetailUseCase = mockk()
    private val updateMovieStateUseCase: UpdateMovieStateUseCase = mockk()
    private val syncScheduler: WorkManagerSyncScheduler = mockk(relaxed = true)
    private val testMovie = createTestMovie()

    private lateinit var viewModel: MovieDetailViewModel

    @BeforeEach
    fun setUp() {
        viewModel =
            MovieDetailViewModel(
                getMovieDetailUseCase = getMovieDetailUseCase,
                updateMovieStateUseCase = updateMovieStateUseCase,
                syncScheduler = syncScheduler,
            )
    }

    @Nested
    inner class FetchMovieDetails {
        @Test
        fun `GIVEN movie id WHEN FetchDetails intent THEN returns expected movie`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.success(testMovie))

                viewModel.uiState.test {
                    awaitItem() shouldBeEqualTo MovieDetailUiState()
                    viewModel.process(MovieDetailIntent.FetchDetails(testMovie.id))
                    awaitItem() shouldBeEqualTo MovieDetailUiState(isLoading = true)
                    awaitItem() shouldBeEqualTo
                        MovieDetailUiState(
                            isLoading = false,
                            movie = testMovie.toUi(),
                            errorMessage = null,
                        )
                    cancelAndConsumeRemainingEvents()
                }
            }

        @Test
        fun `GIVEN error WHEN FetchDetails intent THEN update state with error`() =
            runTest {
                val errorMessage = "Error occurred"
                coEvery { getMovieDetailUseCase(testMovie.id) } returns
                    flowOf(Result.failure(Exception(errorMessage)))

                viewModel.uiState.test {
                    awaitItem() shouldBeEqualTo MovieDetailUiState()
                    viewModel.process(MovieDetailIntent.FetchDetails(testMovie.id))
                    awaitItem() shouldBeEqualTo MovieDetailUiState(isLoading = true)
                    awaitItem() shouldBeEqualTo
                        MovieDetailUiState(
                            isLoading = false,
                            movie = null,
                            errorMessage = errorMessage,
                        )
                    cancelAndConsumeRemainingEvents()
                }
            }

        @Test
        fun `GIVEN loading state WHEN FetchDetails intent THEN update state to loading`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.success(testMovie))

                viewModel.uiState.test {
                    awaitItem() shouldBeEqualTo MovieDetailUiState()
                    viewModel.process(MovieDetailIntent.FetchDetails(testMovie.id))
                    awaitItem() shouldBeEqualTo MovieDetailUiState(isLoading = true)
                    cancelAndConsumeRemainingEvents()
                }
            }
    }

    @Nested
    inner class ToggleWatchlist {
        @Test
        fun `GIVEN movie WHEN ToggleWatchlist intent succeeds THEN update movie watchlist state`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.success(true)

                viewModel.process(MovieDetailIntent.FetchDetails(testMovie.id))
                advanceUntilIdle()

                viewModel.process(MovieDetailIntent.ToggleWatchlist)

                viewModel.uiState.test {
                    val expectedMovie = testMovie.toUi().copy(isInWatchlist = true)
                    awaitItem().movie shouldBeEqualTo expectedMovie
                    cancelAndConsumeRemainingEvents()
                }

                coVerify { updateMovieStateUseCase(testMovie.copy(isInWatchlist = true)) }
            }

        @Test
        fun `GIVEN movie WHEN ToggleWatchlist intent fails THEN update state with error`() =
            runTest {
                val errorMessage = "Failed to update watchlist"
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.failure(Exception(errorMessage))

                viewModel.process(MovieDetailIntent.FetchDetails(testMovie.id))
                advanceUntilIdle()

                viewModel.process(MovieDetailIntent.ToggleWatchlist)

                viewModel.uiState.test {
                    val state = awaitItem()
                    state.errorMessage shouldBeEqualTo errorMessage
                    state.movie shouldBeEqualTo testMovie.toUi()
                    cancelAndConsumeRemainingEvents()
                }
            }

        @Test
        fun `GIVEN movie WHEN ToggleWatchlist intent succeeds THEN enqueues upload`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.success(true)

                viewModel.process(MovieDetailIntent.FetchDetails(testMovie.id))
                advanceUntilIdle()
                viewModel.process(MovieDetailIntent.ToggleWatchlist)
                advanceUntilIdle()

                coVerify(exactly = 1) { syncScheduler.enqueueUpload(testMovie.id) }
            }

        @Test
        fun `GIVEN movie WHEN ToggleWatchlist intent fails THEN does not enqueue upload`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.failure(Exception("error"))

                viewModel.process(MovieDetailIntent.FetchDetails(testMovie.id))
                advanceUntilIdle()
                viewModel.process(MovieDetailIntent.ToggleWatchlist)
                advanceUntilIdle()

                coVerify(exactly = 0) { syncScheduler.enqueueUpload(any()) }
            }
    }

    @Nested
    inner class ToggleWatched {
        @Test
        fun `GIVEN movie WHEN ToggleWatched intent succeeds THEN update movie watched state`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.success(true)

                viewModel.process(MovieDetailIntent.FetchDetails(testMovie.id))
                advanceUntilIdle()

                viewModel.process(MovieDetailIntent.ToggleWatched)

                viewModel.uiState.test {
                    val actualMovie = awaitItem().movie
                    val expectedMovie =
                        testMovie.toUi().copy(
                            isWatched = true,
                            watchedAt = actualMovie?.watchedAt,
                        )

                    actualMovie shouldBeEqualTo expectedMovie
                    if (actualMovie?.watchedAt == null) {
                        throw AssertionError("watchedAt should not be null when marked as watched")
                    }
                    cancelAndConsumeRemainingEvents()
                }

                coVerify { updateMovieStateUseCase(any()) }
            }

        @Test
        fun `GIVEN movie WHEN ToggleWatched intent fails THEN update state with error`() =
            runTest {
                val errorMessage = "Failed to update watched status"
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.failure(Exception(errorMessage))

                viewModel.process(MovieDetailIntent.FetchDetails(testMovie.id))
                advanceUntilIdle()

                viewModel.process(MovieDetailIntent.ToggleWatched)

                viewModel.uiState.test {
                    val state = awaitItem()
                    state.errorMessage shouldBeEqualTo errorMessage
                    state.movie shouldBeEqualTo testMovie.toUi()
                    cancelAndConsumeRemainingEvents()
                }
            }

        @Test
        fun `GIVEN movie WHEN ToggleWatched intent succeeds THEN enqueues upload`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.success(true)

                viewModel.process(MovieDetailIntent.FetchDetails(testMovie.id))
                advanceUntilIdle()
                viewModel.process(MovieDetailIntent.ToggleWatched)
                advanceUntilIdle()

                coVerify(exactly = 1) { syncScheduler.enqueueUpload(testMovie.id) }
            }

        @Test
        fun `GIVEN movie WHEN ToggleWatched intent fails THEN does not enqueue upload`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.failure(Exception("error"))

                viewModel.process(MovieDetailIntent.FetchDetails(testMovie.id))
                advanceUntilIdle()
                viewModel.process(MovieDetailIntent.ToggleWatched)
                advanceUntilIdle()

                coVerify(exactly = 0) { syncScheduler.enqueueUpload(any()) }
            }
    }

    private fun createTestMovie() =
        Movie(
            id = 1,
            title = "Inception",
            overview = "A skilled thief is offered a chance to erase his criminal record.",
            posterPath = "https://image.tmdb.org/t/p/w500/path/to/poster.jpg",
            backdropPath = null,
            releaseDate = "2010-07-16",
            popularity = 80.0,
            voteAverage = 8.8,
            voteCount = 20000,
            genres = listOf(),
            productionCountries = listOf(),
            cast = listOf(),
            isWatched = false,
            isInWatchlist = false,
            updatedAt = 0L,
        )
}
