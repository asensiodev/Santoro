package com.asensiodev.feature.watchedmovies.impl.presentation

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.watchedmovies.impl.domain.model.WatchedStats
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedStatsUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.SearchWatchedMoviesUseCase
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
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchedMoviesViewModelTest {
    private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase = mockk(relaxed = true)
    private val getWatchedStatsUseCase: GetWatchedStatsUseCase = mockk(relaxed = true)
    private val searchWatchedMoviesUseCase: SearchWatchedMoviesUseCase = mockk(relaxed = true)

    private lateinit var viewModel: WatchedMoviesViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getWatchedMoviesUseCase() } returns flowOf(Result.success(emptyList()))
        every { getWatchedStatsUseCase() } returns
            flowOf(
                WatchedStats(
                    totalWatched = 0,
                    totalRuntimeHours = 0,
                    favouriteGenre = null,
                    longestStreakWeeks = 0,
                ),
            )
        viewModel =
            WatchedMoviesViewModel(
                getWatchedMoviesUseCase = getWatchedMoviesUseCase,
                getWatchedStatsUseCase = getWatchedStatsUseCase,
                searchWatchedMoviesUseCase = searchWatchedMoviesUseCase,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN LoadMovies intent WHEN processed THEN isLoading becomes false after fetch`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.uiState.value.isLoading shouldBeEqualTo false
        }

    @Test
    fun `GIVEN UpdateQuery intent WHEN processed THEN query is updated in state`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.UpdateQuery("inception"))

            viewModel.uiState.value.query shouldBeEqualTo "inception"
        }

    @Test
    fun `GIVEN use case returns movies WHEN LoadMovies intent THEN movies are shown`() =
        runTest {
            val movie = buildMovie(id = 1, title = "Inception")
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(movie)))
            viewModel =
                WatchedMoviesViewModel(
                    getWatchedMoviesUseCase = getWatchedMoviesUseCase,
                    getWatchedStatsUseCase = getWatchedStatsUseCase,
                    searchWatchedMoviesUseCase = searchWatchedMoviesUseCase,
                )

            advanceUntilIdle()

            viewModel.uiState.value.movies
                .shouldNotBeNull()
        }

    @Test
    fun `GIVEN LoadMovies intent WHEN processed THEN stats are populated in state`() =
        runTest {
            val expectedStats =
                WatchedStats(
                    totalWatched = 2,
                    totalRuntimeHours = 3,
                    favouriteGenre = "Action",
                    longestStreakWeeks = 1,
                )
            every { getWatchedStatsUseCase() } returns flowOf(expectedStats)
            viewModel =
                WatchedMoviesViewModel(
                    getWatchedMoviesUseCase = getWatchedMoviesUseCase,
                    getWatchedStatsUseCase = getWatchedStatsUseCase,
                    searchWatchedMoviesUseCase = searchWatchedMoviesUseCase,
                )

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.uiState.value.stats shouldBeEqualTo expectedStats
        }

    @Test
    fun `GIVEN stats use case emits error stats WHEN LoadMovies THEN stats remain available without crashing`() =
        runTest {
            val zeroStats =
                WatchedStats(
                    totalWatched = 0,
                    totalRuntimeHours = 0,
                    favouriteGenre = null,
                    longestStreakWeeks = 0,
                )
            every { getWatchedStatsUseCase() } returns flowOf(zeroStats)
            viewModel =
                WatchedMoviesViewModel(
                    getWatchedMoviesUseCase = getWatchedMoviesUseCase,
                    getWatchedStatsUseCase = getWatchedStatsUseCase,
                    searchWatchedMoviesUseCase = searchWatchedMoviesUseCase,
                )

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.uiState.value.stats
                .shouldNotBeNull()
            viewModel.uiState.value.errorMessage
                .shouldBeNull()
        }

    private fun buildMovie(
        id: Int,
        title: String = "Movie $id",
        genres: List<Genre> = emptyList(),
    ): Movie =
        Movie(
            id = id,
            title = title,
            overview = "",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            popularity = 0.0,
            voteAverage = 0.0,
            voteCount = 0,
            genres = genres,
            productionCountries = emptyList(),
            runtime = null,
            director = null,
            isWatched = true,
            isInWatchlist = false,
            watchedAt = null,
        )
}
