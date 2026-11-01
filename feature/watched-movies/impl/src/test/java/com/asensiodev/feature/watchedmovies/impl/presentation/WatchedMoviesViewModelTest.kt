package com.asensiodev.feature.watchedmovies.impl.presentation

import com.asensiodev.core.domain.model.Director
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
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
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class WatchedMoviesViewModelTest {
    private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase = mockk(relaxed = true)
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
        viewModel =
            WatchedMoviesViewModel(
                getWatchedMoviesUseCase = getWatchedMoviesUseCase,
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
    fun `GIVEN movie with null watchedAt WHEN LoadMovies THEN groups under unknown date`() =
        runTest {
            val movie = buildMovie(id = 1).copy(watchedAt = null)
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(movie)))
            viewModel =
                WatchedMoviesViewModel(
                    getWatchedMoviesUseCase = getWatchedMoviesUseCase,
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
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(movie)))
            every { searchWatchedMoviesUseCase("test") } returns flowOf(Result.success(listOf(movie)))
            viewModel =
                WatchedMoviesViewModel(
                    getWatchedMoviesUseCase = getWatchedMoviesUseCase,
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
    fun `GIVEN repeated LoadMovies intents WHEN processed THEN movie observer starts once`() =
        runTest {
            var movieSubscriptions = 0
            every { getWatchedMoviesUseCase() } returns
                flow {
                    movieSubscriptions++
                    emit(Result.success(emptyList()))
                    awaitCancellation()
                }

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            runCurrent()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            runCurrent()

            movieSubscriptions shouldBeEqualTo 1
            verify(exactly = 1) { getWatchedMoviesUseCase() }
        }

    @Test
    fun `GIVEN query changes WHEN newer search starts THEN previous search is cancelled`() =
        runTest {
            val firstSearchStarted = CompletableDeferred<Unit>()
            val firstSearchCancelled = CompletableDeferred<Unit>()
            val latestMovie = buildMovie(id = 2, title = "Latest")
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(latestMovie)))
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

    @Test
    fun `GIVEN co-directed movie in director grouping WHEN loaded THEN total counts each movie once`() =
        runTest {
            every { userPreferencesRepository.watchedGrouping } returns flowOf(WatchedGrouping.DIRECTOR)
            every { getWatchedMoviesUseCase() } returns
                flowOf(Result.success(listOf(coenMovie(FARGO_ID), villeneuveMovie(2))))
            viewModel = createViewModel()

            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.uiState.value.groups
                .sumOf { it.movies.size } shouldBeEqualTo 3
            viewModel.uiState.value.totalMovies shouldBeEqualTo 2
        }

    @Test
    fun `GIVEN populated collection WHEN searching THEN total keeps the collection size`() =
        runTest {
            every { getWatchedMoviesUseCase() } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune"), villeneuveMovie(2))))
            every { searchWatchedMoviesUseCase("dune") } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune"))))
            viewModel = createViewModel()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.OpenSearch)
            viewModel.process(WatchedMoviesIntent.UpdateQuery("dune"))
            advanceUntilIdle()

            viewModel.uiState.value.groups
                .flatMap { it.movies }
                .map { it.id } shouldBeEqualTo listOf(1)
            viewModel.uiState.value.totalMovies shouldBeEqualTo 2
        }

    @Test
    fun `GIVEN populated collection WHEN OpenSearch THEN search is open`() =
        runTest {
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(listOf(villeneuveMovie(1))))
            viewModel = createViewModel()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.OpenSearch)

            viewModel.uiState.value.isSearchOpen shouldBeEqualTo true
        }

    @Test
    fun `GIVEN open search with query WHEN CloseSearch THEN query is cleared and full collection returns`() =
        runTest {
            every { getWatchedMoviesUseCase() } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune"), villeneuveMovie(2))))
            every { searchWatchedMoviesUseCase("dune") } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune"))))
            viewModel = createViewModel()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchedMoviesIntent.OpenSearch)
            viewModel.process(WatchedMoviesIntent.UpdateQuery("dune"))
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.CloseSearch)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.isSearchOpen shouldBeEqualTo false
            state.query shouldBeEqualTo ""
            state.groups
                .flatMap { it.movies }
                .map { it.id }
                .sorted() shouldBeEqualTo listOf(1, 2)
        }

    @Test
    fun `GIVEN open search with matches WHEN CloseSearch THEN full collection shows before the query debounce`() =
        runTest {
            every { getWatchedMoviesUseCase() } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune"), villeneuveMovie(2))))
            every { searchWatchedMoviesUseCase("dune") } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune"))))
            viewModel = createViewModel()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchedMoviesIntent.OpenSearch)
            viewModel.process(WatchedMoviesIntent.UpdateQuery("dune"))
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.CloseSearch)
            runCurrent()

            viewModel.uiState.value.groups
                .flatMap { it.movies }
                .map { it.id }
                .sorted() shouldBeEqualTo listOf(1, 2)
        }

    @Test
    fun `GIVEN closed search WHEN the old search emits before the debounce THEN full collection stays`() =
        runTest {
            val collection =
                MutableStateFlow(Result.success(listOf(villeneuveMovie(1, title = "Dune"), villeneuveMovie(2))))
            every { getWatchedMoviesUseCase() } returns collection
            every { searchWatchedMoviesUseCase("dune") } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune"))))
            viewModel = createViewModel()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchedMoviesIntent.OpenSearch)
            viewModel.process(WatchedMoviesIntent.UpdateQuery("dune"))
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.CloseSearch)
            runCurrent()
            collection.value =
                Result.success(listOf(villeneuveMovie(1, title = "Dune"), villeneuveMovie(2), villeneuveMovie(3)))
            runCurrent()

            viewModel.uiState.value.groups
                .flatMap { it.movies }
                .map { it.id }
                .sorted() shouldBeEqualTo listOf(1, 2)
        }

    @Test
    fun `GIVEN search results WHEN grouping changes THEN query is kept and matches are regrouped`() =
        runTest {
            every { getWatchedMoviesUseCase() } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune", releaseDate = "2021-09-15"))))
            every { searchWatchedMoviesUseCase("dune") } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune", releaseDate = "2021-09-15"))))
            viewModel = createViewModel()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchedMoviesIntent.OpenSearch)
            viewModel.process(WatchedMoviesIntent.UpdateQuery("dune"))
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.ChangeGrouping(WatchedGrouping.DECADE))
            advanceUntilIdle()

            viewModel.uiState.value.query shouldBeEqualTo "dune"
            viewModel.uiState.value.groups
                .map { it.label } shouldBeEqualTo listOf(WatchedGroupLabel.Decade(2020))
        }

    @Test
    fun `GIVEN open search WHEN collection becomes empty THEN search closes and Empty is shown`() =
        runTest {
            val collection = MutableStateFlow(Result.success(listOf(villeneuveMovie(1, title = "Dune"))))
            val matches = MutableStateFlow(Result.success(listOf(villeneuveMovie(1, title = "Dune"))))
            every { getWatchedMoviesUseCase() } returns collection
            every { searchWatchedMoviesUseCase("dune") } returns matches
            viewModel = createViewModel()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchedMoviesIntent.OpenSearch)
            viewModel.process(WatchedMoviesIntent.UpdateQuery("dune"))
            advanceUntilIdle()

            collection.value = Result.success(emptyList())
            matches.value = Result.success(emptyList())
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.isSearchOpen shouldBeEqualTo false
            state.query shouldBeEqualTo ""
            state.screenState shouldBeEqualTo WatchedScreenState.Empty
        }

    private fun coenMovie(id: Int) =
        buildMovie(id, "Fargo").copy(
            directors = listOf(Director(JOEL_COEN_ID, "Joel Coen"), Director(ETHAN_COEN_ID, "Ethan Coen")),
        )

    private fun createViewModel() =
        WatchedMoviesViewModel(
            getWatchedMoviesUseCase = getWatchedMoviesUseCase,
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
        const val JOEL_COEN_ID = 1223
        const val ETHAN_COEN_ID = 1224
        const val FARGO_ID = 275
    }
}
