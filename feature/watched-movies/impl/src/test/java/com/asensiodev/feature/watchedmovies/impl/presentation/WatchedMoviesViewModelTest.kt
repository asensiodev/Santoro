package com.asensiodev.feature.watchedmovies.impl.presentation

import com.asensiodev.core.domain.model.Director
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.feature.watchedmovies.impl.domain.model.WatchedStats
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedStatsUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.SearchWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupLabel
import com.asensiodev.library.observability.api.ObservabilityTracker
import io.mockk.coEvery
import io.mockk.coVerify
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class WatchedMoviesViewModelTest {
    private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase = mockk(relaxed = true)
    private val getWatchedStatsUseCase: GetWatchedStatsUseCase = mockk(relaxed = true)
    private val searchWatchedMoviesUseCase: SearchWatchedMoviesUseCase = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val observabilityTracker: ObservabilityTracker = mockk(relaxed = true)

    private lateinit var viewModel: WatchedMoviesViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPreferencesRepository.watchedViewMode } returns flowOf(WatchedViewMode.SHELF)
        every { userPreferencesRepository.watchedGrouping } returns flowOf(WatchedGrouping.DATE_WATCHED)
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
                userPreferencesRepository = userPreferencesRepository,
                observabilityTracker = observabilityTracker,
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
                    userPreferencesRepository = userPreferencesRepository,
                    observabilityTracker = observabilityTracker,
                )

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.uiState.value.groups
                .flatMap { it.movies }
                .map { it.id } shouldBeEqualTo listOf(1)
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
                    userPreferencesRepository = userPreferencesRepository,
                    observabilityTracker = observabilityTracker,
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
                    userPreferencesRepository = userPreferencesRepository,
                    observabilityTracker = observabilityTracker,
                )

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.uiState.value.stats
                .shouldNotBeNull()
            viewModel.uiState.value.screenState shouldBeEqualTo WatchedScreenState.Empty
        }

    @Test
    fun `GIVEN movie with null watchedAt WHEN LoadMovies THEN groups under unknown date`() =
        runTest {
            val movie = buildMovie(id = 1).copy(watchedAt = null)
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(movie)))
            viewModel =
                WatchedMoviesViewModel(
                    getWatchedMoviesUseCase = getWatchedMoviesUseCase,
                    getWatchedStatsUseCase = getWatchedStatsUseCase,
                    searchWatchedMoviesUseCase = searchWatchedMoviesUseCase,
                    userPreferencesRepository = userPreferencesRepository,
                    observabilityTracker = observabilityTracker,
                )

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.uiState.value.groups
                .map { it.label } shouldBeEqualTo listOf(WatchedGroupLabel.UnknownDate)
        }

    @Test
    fun `GIVEN search movie with null watchedAt WHEN searching THEN groups under unknown date`() =
        runTest {
            val movie = buildMovie(id = 1).copy(watchedAt = null)
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(emptyList()))
            every { searchWatchedMoviesUseCase("test") } returns flowOf(Result.success(listOf(movie)))
            viewModel =
                WatchedMoviesViewModel(
                    getWatchedMoviesUseCase = getWatchedMoviesUseCase,
                    getWatchedStatsUseCase = getWatchedStatsUseCase,
                    searchWatchedMoviesUseCase = searchWatchedMoviesUseCase,
                    userPreferencesRepository = userPreferencesRepository,
                    observabilityTracker = observabilityTracker,
                )

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchedMoviesIntent.UpdateQuery("test"))
            advanceUntilIdle()

            viewModel.uiState.value.groups
                .map { it.label } shouldBeEqualTo listOf(WatchedGroupLabel.UnknownDate)
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
                    userPreferencesRepository = userPreferencesRepository,
                    observabilityTracker = observabilityTracker,
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
            viewModel.uiState.value.groups
                .flatMap { it.movies }
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

    @Test
    fun `GIVEN saved list mode and director grouping WHEN movies load THEN first content uses them`() =
        runTest {
            every { userPreferencesRepository.watchedViewMode } returns flowOf(WatchedViewMode.LIST)
            every { userPreferencesRepository.watchedGrouping } returns flowOf(WatchedGrouping.DIRECTOR)
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(villeneuveMovie(1))))
            viewModel = createViewModel()

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.viewMode shouldBeEqualTo WatchedViewMode.LIST
            state.grouping shouldBeEqualTo WatchedGrouping.DIRECTOR
            state.groups.map { it.label } shouldBeEqualTo listOf(WatchedGroupLabel.Text("Denis Villeneuve"))
        }

    @Test
    fun `GIVEN loaded movies WHEN grouping changes THEN regroups persists and tracks the change`() =
        runTest {
            every { getWatchedMoviesUseCase() } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, releaseDate = "2016-11-11"))))
            viewModel = createViewModel()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.ChangeGrouping(WatchedGrouping.DECADE))
            advanceUntilIdle()

            viewModel.uiState.value.grouping shouldBeEqualTo WatchedGrouping.DECADE
            viewModel.uiState.value.groups
                .map { it.label } shouldBeEqualTo listOf(WatchedGroupLabel.Decade(2010))
            coVerify(exactly = 1) { userPreferencesRepository.setWatchedGrouping(WatchedGrouping.DECADE) }
            verify(exactly = 1) {
                observabilityTracker.trackAction("watched_grouping_changed", mapOf("grouping" to "decade"))
            }
        }

    @Test
    fun `GIVEN view mode changes WHEN processed THEN state persists and tracks the change`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.ChangeViewMode(WatchedViewMode.LIST))
            advanceUntilIdle()

            viewModel.uiState.value.viewMode shouldBeEqualTo WatchedViewMode.LIST
            coVerify(exactly = 1) { userPreferencesRepository.setWatchedViewMode(WatchedViewMode.LIST) }
            verify(exactly = 1) {
                observabilityTracker.trackAction("watched_view_mode_changed", mapOf("mode" to "list"))
            }
        }

    @Test
    fun `GIVEN preferences cannot be read WHEN movies load THEN defaults are used`() =
        runTest {
            every { userPreferencesRepository.watchedViewMode } returns flow { throw IOException("Unreadable") }
            every { userPreferencesRepository.watchedGrouping } returns flow { throw IOException("Unreadable") }
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(villeneuveMovie(1))))
            viewModel = createViewModel()

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.screenState shouldBeEqualTo WatchedScreenState.Content
            state.viewMode shouldBeEqualTo WatchedViewMode.SHELF
            state.grouping shouldBeEqualTo WatchedGrouping.DATE_WATCHED
        }

    @Test
    fun `GIVEN preference cannot be saved WHEN grouping changes THEN choice is kept for the session`() =
        runTest {
            coEvery { userPreferencesRepository.setWatchedGrouping(any()) } throws IOException("Full disk")
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(villeneuveMovie(1))))
            viewModel = createViewModel()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.ChangeGrouping(WatchedGrouping.DIRECTOR))
            advanceUntilIdle()

            viewModel.uiState.value.grouping shouldBeEqualTo WatchedGrouping.DIRECTOR
            viewModel.uiState.value.groups
                .map { it.label } shouldBeEqualTo
                listOf(WatchedGroupLabel.Text("Denis Villeneuve"))
        }

    @Test
    fun `GIVEN director grouping WHEN search results arrive THEN results are grouped by director`() =
        runTest {
            every { userPreferencesRepository.watchedGrouping } returns flowOf(WatchedGrouping.DIRECTOR)
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(villeneuveMovie(1))))
            every { searchWatchedMoviesUseCase("dune") } returns
                flowOf(Result.success(listOf(villeneuveMovie(2, title = "Dune"))))
            viewModel = createViewModel()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.UpdateQuery("dune"))
            advanceUntilIdle()

            viewModel.uiState.value.groups
                .single()
                .movies
                .map { it.id } shouldBeEqualTo listOf(2)
            viewModel.uiState.value.groups
                .single()
                .directorId shouldBeEqualTo VILLENEUVE_ID
        }

    @Test
    fun `GIVEN populated collection WHEN content emits repeatedly THEN collection view is tracked once`() =
        runTest {
            every { userPreferencesRepository.watchedViewMode } returns flowOf(WatchedViewMode.LIST)
            every { userPreferencesRepository.watchedGrouping } returns flowOf(WatchedGrouping.DIRECTOR)
            every { getWatchedMoviesUseCase() } returns
                flowOf(Result.success(listOf(villeneuveMovie(1))), Result.success(listOf(villeneuveMovie(2))))
            viewModel = createViewModel()

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            verify(exactly = 1) {
                observabilityTracker.trackAction(
                    "watched_collection_viewed",
                    mapOf("mode" to "list", "grouping" to "director"),
                )
            }
        }

    @Test
    fun `GIVEN empty collection WHEN loaded THEN collection view is not tracked`() =
        runTest {
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            verify(exactly = 0) { observabilityTracker.trackAction("watched_collection_viewed", any()) }
        }

    private fun createViewModel() =
        WatchedMoviesViewModel(
            getWatchedMoviesUseCase = getWatchedMoviesUseCase,
            getWatchedStatsUseCase = getWatchedStatsUseCase,
            searchWatchedMoviesUseCase = searchWatchedMoviesUseCase,
            userPreferencesRepository = userPreferencesRepository,
            observabilityTracker = observabilityTracker,
        )

    private fun villeneuveMovie(
        id: Int,
        title: String = "Movie $id",
        releaseDate: String? = null,
    ) = buildMovie(id, title).copy(
        releaseDate = releaseDate,
        directors = listOf(Director(VILLENEUVE_ID, "Denis Villeneuve")),
    )

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

    private companion object {
        const val VILLENEUVE_ID = 137427
    }
}
