package com.asensiodev.feature.watchedmovies.impl.presentation

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.watchedmovies.impl.domain.model.WatchedStats
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedStatsUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.SearchWatchedMoviesUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
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
    fun `GIVEN LoadMovies intent WHEN processed THEN screenState becomes Empty after fetch`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.uiState.value.screenState shouldBeEqualTo WatchedScreenState.Empty
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
                    longestStreakWeeks = 2,
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
            viewModel.uiState.value.screenState shouldBeEqualTo WatchedScreenState.Empty
        }

    @Test
    fun `GIVEN movie with null watchedAt WHEN LoadMovies THEN groups under blank key not hardcoded Unknown`() =
        runTest {
            val movie = buildMovie(id = 1).copy(watchedAt = null)
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(movie)))
            viewModel =
                WatchedMoviesViewModel(
                    getWatchedMoviesUseCase = getWatchedMoviesUseCase,
                    getWatchedStatsUseCase = getWatchedStatsUseCase,
                    searchWatchedMoviesUseCase = searchWatchedMoviesUseCase,
                )

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            val keys = viewModel.uiState.value.movies.keys
            keys.none { it == "Unknown" } shouldBeEqualTo true
        }

    @Test
    fun `GIVEN search movie with null watchedAt WHEN searching THEN groups under blank key`() =
        runTest {
            val movie = buildMovie(id = 1).copy(watchedAt = null)
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(emptyList()))
            every { searchWatchedMoviesUseCase("test") } returns flowOf(Result.success(listOf(movie)))
            viewModel =
                WatchedMoviesViewModel(
                    getWatchedMoviesUseCase = getWatchedMoviesUseCase,
                    getWatchedStatsUseCase = getWatchedStatsUseCase,
                    searchWatchedMoviesUseCase = searchWatchedMoviesUseCase,
                )

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchedMoviesIntent.UpdateQuery("test"))
            advanceUntilIdle()

            val keys = viewModel.uiState.value.movies.keys
            keys.none { it == "Unknown" } shouldBeEqualTo true
        }

    @Test
    fun `GIVEN watched movies exist WHEN search returns empty THEN screenState becomes NoResults`() =
        runTest {
            val movie = buildMovie(id = 1, title = "Inception")
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(movie)))
            every { searchWatchedMoviesUseCase("matrix") } returns flowOf(Result.success(emptyList()))
            viewModel =
                WatchedMoviesViewModel(
                    getWatchedMoviesUseCase = getWatchedMoviesUseCase,
                    getWatchedStatsUseCase = getWatchedStatsUseCase,
                    searchWatchedMoviesUseCase = searchWatchedMoviesUseCase,
                )

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchedMoviesIntent.UpdateQuery("matrix"))
            advanceTimeBy(500)
            advanceUntilIdle()

            viewModel.uiState.value.screenState shouldBeEqualTo WatchedScreenState.NoResults
        }

    @Test
    fun `GIVEN repeated LoadMovies intents WHEN processed THEN movie and stats observers start once`() =
        runTest {
            var movieSubscriptions = 0
            var statsSubscriptions = 0
            every { getWatchedMoviesUseCase() } returns
                flow {
                    movieSubscriptions++
                    emit(Result.success(emptyList()))
                    awaitCancellation()
                }
            every { getWatchedStatsUseCase() } returns
                flow {
                    statsSubscriptions++
                    emit(WatchedStats(0, 0, null, 0))
                    awaitCancellation()
                }

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            runCurrent()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            runCurrent()

            movieSubscriptions shouldBeEqualTo 1
            statsSubscriptions shouldBeEqualTo 1
            verify(exactly = 1) { getWatchedMoviesUseCase() }
            verify(exactly = 1) { getWatchedStatsUseCase() }
        }

    @Test
    fun `GIVEN query changes WHEN newer search starts THEN previous search is cancelled`() =
        runTest {
            val firstSearchStarted = CompletableDeferred<Unit>()
            val firstSearchCancelled = CompletableDeferred<Unit>()
            val latestMovie = buildMovie(id = 2, title = "Latest")
            every { searchWatchedMoviesUseCase("first") } returns
                flow {
                    firstSearchStarted.complete(Unit)
                    try {
                        awaitCancellation()
                    } finally {
                        firstSearchCancelled.complete(Unit)
                    }
                }
            every { searchWatchedMoviesUseCase("latest") } returns
                flowOf(Result.success(listOf(latestMovie)))

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            runCurrent()
            viewModel.process(WatchedMoviesIntent.UpdateQuery("first"))
            advanceTimeBy(500)
            runCurrent()
            firstSearchStarted.await()

            viewModel.process(WatchedMoviesIntent.UpdateQuery("latest"))
            advanceTimeBy(500)
            advanceUntilIdle()

            firstSearchCancelled.await()
            viewModel.uiState.value.movies.values
                .flatten()
                .map { it.id } shouldBeEqualTo listOf(2)
        }

    @Test
    fun `GIVEN no watched movies WHEN search returns empty THEN screenState remains Empty`() =
        runTest {
            every { searchWatchedMoviesUseCase("missing") } returns flowOf(Result.success(emptyList()))

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchedMoviesIntent.UpdateQuery("missing"))
            advanceTimeBy(500)
            advanceUntilIdle()

            viewModel.uiState.value.screenState shouldBeEqualTo WatchedScreenState.Empty
        }

    @Test
    fun `GIVEN movie observation failed WHEN LoadMovies retries THEN observer is replaced`() =
        runTest {
            every { getWatchedMoviesUseCase() } returnsMany
                listOf(
                    flowOf(Result.failure(Exception())),
                    flowOf(Result.success(listOf(buildMovie(id = 1)))),
                )

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            verify(exactly = 2) { getWatchedMoviesUseCase() }
            viewModel.uiState.value.screenState shouldBeEqualTo WatchedScreenState.Content
        }

    @Test
    fun `GIVEN observation throws cancellation WHEN loading THEN error state is not created`() =
        runTest {
            every {
                getWatchedMoviesUseCase()
            } returns flow { throw CancellationException() }

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.uiState.value.screenState shouldBeEqualTo WatchedScreenState.Loading
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
