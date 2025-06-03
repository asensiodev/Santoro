package com.asensiodev.feature.moviedetail.impl.presentation

import app.cash.turbine.test
import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.extension.CoroutineTestExtension
import com.asensiodev.feature.moviedetail.impl.domain.usecase.GetMovieDetailUseCase
import com.asensiodev.feature.moviedetail.impl.domain.usecase.UpdateMovieStateUseCase
import com.asensiodev.feature.moviedetail.impl.presentation.mapper.toUi
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
    private val testMovie = createTestMovie()

    private lateinit var viewModel: MovieDetailViewModel

    @BeforeEach
    fun setUp() {
        viewModel =
            MovieDetailViewModel(
                getMovieDetailUseCase = getMovieDetailUseCase,
                updateMovieStateUseCase = updateMovieStateUseCase,
            )
    }

    @Nested
    inner class FetchMovieDetails {
        @Test
        fun `GIVEN movie id WHEN fetchMovieDetails THEN returns expected movie`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.Success(testMovie))

                viewModel.uiState.test {
                    awaitItem() shouldBeEqualTo MovieDetailUiState()
                    viewModel.fetchMovieDetails(testMovie.id)
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
        fun `GIVEN error WHEN fetchMovieDetails THEN update state with error`() =
            runTest {
                val errorMessage = "Error occurred"
                coEvery { getMovieDetailUseCase(testMovie.id) } returns
                    flowOf(
                        Result.Error(Exception(errorMessage)),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBeEqualTo MovieDetailUiState()
                    viewModel.fetchMovieDetails(testMovie.id)
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
        fun `GIVEN loading state WHEN fetchMovieDetails THEN update state to loading`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.Loading)

                viewModel.uiState.test {
                    awaitItem() shouldBeEqualTo MovieDetailUiState()
                    viewModel.fetchMovieDetails(testMovie.id)
                    awaitItem() shouldBeEqualTo MovieDetailUiState(isLoading = true)
                    cancelAndConsumeRemainingEvents()
                }
            }
    }

    @Nested
    inner class ToggleWatchlist {
        @Test
        fun `GIVEN movie WHEN toggleWatchlist succeeds THEN update movie watchlist state`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.Success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.Success(true)

                viewModel.fetchMovieDetails(testMovie.id)

                advanceUntilIdle()

                viewModel.toggleWatchlist()

                viewModel.uiState.test {
                    val expectedMovie = testMovie.toUi().copy(isInWatchlist = true)
                    awaitItem().movie shouldBeEqualTo expectedMovie
                    cancelAndConsumeRemainingEvents()
                }

                coVerify { updateMovieStateUseCase(testMovie.copy(isInWatchlist = true)) }
            }

        @Test
        fun `GIVEN movie WHEN toggleWatchlist fails THEN update state with error`() =
            runTest {
                val errorMessage = "Failed to update watchlist"
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.Success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.Error(Exception(errorMessage))

                viewModel.fetchMovieDetails(testMovie.id)
                advanceUntilIdle()

                viewModel.toggleWatchlist()

                viewModel.uiState.test {
                    val state = awaitItem()
                    state.errorMessage shouldBeEqualTo errorMessage
                    state.movie shouldBeEqualTo testMovie.toUi()
                    cancelAndConsumeRemainingEvents()
                }
            }
    }

    @Nested
    inner class ToggleWatched {
        @Test
        fun `GIVEN movie WHEN toggleWatched succeeds THEN update movie watched state`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.Success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.Success(true)

                viewModel.fetchMovieDetails(testMovie.id)
                advanceUntilIdle()

                viewModel.toggleWatched()

                viewModel.uiState.test {
                    val expectedMovie = testMovie.toUi().copy(isWatched = true)
                    awaitItem().movie shouldBeEqualTo expectedMovie
                    cancelAndConsumeRemainingEvents()
                }

                coVerify { updateMovieStateUseCase(testMovie.copy(isWatched = true)) }
            }

        @Test
        fun `GIVEN movie WHEN toggleWatched fails THEN update state with error`() =
            runTest {
                val errorMessage = "Failed to update watched status"
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.Success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.Error(Exception(errorMessage))

                viewModel.fetchMovieDetails(testMovie.id)
                advanceUntilIdle()

                viewModel.toggleWatched()

                viewModel.uiState.test {
                    val state = awaitItem()
                    state.errorMessage shouldBeEqualTo errorMessage
                    state.movie shouldBeEqualTo testMovie.toUi()
                    cancelAndConsumeRemainingEvents()
                }
            }

        @Test
        fun `GIVEN movie WHEN toggleWatched with loading state THEN show loading`() =
            runTest {
                coEvery { getMovieDetailUseCase(testMovie.id) } returns flowOf(Result.Success(testMovie))
                coEvery { updateMovieStateUseCase(any()) } returns Result.Loading

                viewModel.fetchMovieDetails(testMovie.id)
                advanceUntilIdle()

                viewModel.toggleWatched()

                viewModel.uiState.test {
                    awaitItem().isLoading shouldBeEqualTo true
                    cancelAndConsumeRemainingEvents()
                }
            }
    }

    private fun createTestMovie() =
        Movie(
            id = 1,
            title = "Inception",
            overview = "A skilled thief is offered a chance to erase his criminal record.",
            posterPath = "https://image.tmdb.org/t/p/w500/path/to/poster.jpg",
            releaseDate = "2010-07-16",
            popularity = 80.0,
            voteAverage = 8.8,
            voteCount = 20000,
            genres = listOf(),
            productionCountries = listOf(),
            isWatched = false,
            isInWatchlist = false,
        )
}
