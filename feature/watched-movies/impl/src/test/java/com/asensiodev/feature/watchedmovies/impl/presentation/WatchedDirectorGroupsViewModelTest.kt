package com.asensiodev.feature.watchedmovies.impl.presentation

import com.asensiodev.core.domain.model.Director
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.SearchWatchedMoviesUseCase
import com.asensiodev.library.observability.api.ObservabilityTracker
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchedDirectorGroupsViewModelTest {
    private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase = mockk(relaxed = true)
    private val searchWatchedMoviesUseCase: SearchWatchedMoviesUseCase = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val observabilityTracker: ObservabilityTracker = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: WatchedMoviesViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPreferencesRepository.watchedViewMode } returns flowOf(WatchedViewMode.SHELF)
        every { userPreferencesRepository.watchedGrouping } returns flowOf(WatchedGrouping.RECENT)
        every { getWatchedMoviesUseCase() } returns flowOf(Result.success(emptyList()))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN director grouping WHEN movies load THEN director groups start collapsed`() =
        runTest {
            viewModel = loadedDirectorViewModel(villeneuveMovie(1), nolanMovie(2), buildMovie(3))

            viewModel.uiState.value.groups
                .map { Triple(it.key, it.isCollapsible, it.isExpanded) } shouldBeEqualTo
                listOf(
                    Triple(NOLAN_KEY, true, false),
                    Triple(VILLENEUVE_KEY, true, false),
                    Triple(UNKNOWN_DIRECTOR_KEY, true, false),
                )
        }

    @Test
    fun `GIVEN collapsed director WHEN ToggleGroup twice THEN expands and collapses it`() =
        runTest {
            viewModel = loadedDirectorViewModel(villeneuveMovie(1), nolanMovie(2))

            viewModel.process(WatchedMoviesIntent.ToggleGroup(NOLAN_KEY))
            advanceUntilIdle()
            viewModel.expandedKeys() shouldBeEqualTo listOf(NOLAN_KEY)

            viewModel.process(WatchedMoviesIntent.ToggleGroup(NOLAN_KEY))
            advanceUntilIdle()
            viewModel.expandedKeys() shouldBeEqualTo emptyList()
        }

    @Test
    fun `GIVEN expanded director WHEN a new director appears THEN the new group starts collapsed`() =
        runTest {
            val watched = MutableStateFlow(Result.success(listOf(villeneuveMovie(1))))
            every { getWatchedMoviesUseCase() } returns watched
            viewModel = loadedDirectorViewModel()
            viewModel.process(WatchedMoviesIntent.ToggleGroup(VILLENEUVE_KEY))
            advanceUntilIdle()

            watched.value = Result.success(listOf(villeneuveMovie(1), nolanMovie(2)))
            advanceUntilIdle()

            viewModel.uiState.value.groups
                .map { it.key to it.isExpanded }
                .toSet() shouldBeEqualTo
                setOf(VILLENEUVE_KEY to true, NOLAN_KEY to false)
        }

    @Test
    fun `GIVEN collapsed directors WHEN searching THEN matches are expanded and clearing restores them`() =
        runTest {
            every { searchWatchedMoviesUseCase("dune") } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune"))))
            viewModel = loadedDirectorViewModel(villeneuveMovie(1, title = "Dune"), nolanMovie(2))
            viewModel.process(WatchedMoviesIntent.ToggleGroup(NOLAN_KEY))
            advanceUntilIdle()
            viewModel.process(WatchedMoviesIntent.OpenSearch)

            viewModel.process(WatchedMoviesIntent.UpdateQuery("dune"))
            advanceUntilIdle()
            viewModel.uiState.value.groups
                .map { it.key to it.isExpanded } shouldBeEqualTo
                listOf(VILLENEUVE_KEY to true)

            viewModel.process(WatchedMoviesIntent.UpdateQuery(""))
            advanceUntilIdle()
            viewModel.uiState.value.groups
                .map { it.key to it.isExpanded }
                .toSet() shouldBeEqualTo
                setOf(VILLENEUVE_KEY to false, NOLAN_KEY to true)
        }

    @Test
    fun `GIVEN search results WHEN the query is cleared THEN toggling waits for the full collection`() =
        runTest {
            every { searchWatchedMoviesUseCase("dune") } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune"))))
            viewModel = loadedDirectorViewModel(villeneuveMovie(1, title = "Dune"), nolanMovie(2))
            viewModel.process(WatchedMoviesIntent.OpenSearch)
            viewModel.process(WatchedMoviesIntent.UpdateQuery("dune"))
            advanceUntilIdle()
            viewModel.uiState.value.groups
                .map { it.isCollapsible } shouldBeEqualTo listOf(false)

            viewModel.process(WatchedMoviesIntent.UpdateQuery(""))
            viewModel.process(WatchedMoviesIntent.ToggleGroup(VILLENEUVE_KEY))
            advanceUntilIdle()

            viewModel.uiState.value.groups
                .map { Triple(it.key, it.isCollapsible, it.isExpanded) }
                .toSet() shouldBeEqualTo
                setOf(Triple(VILLENEUVE_KEY, true, false), Triple(NOLAN_KEY, true, false))
        }

    @Test
    fun `GIVEN expanded director WHEN it leaves the collection and returns THEN it starts collapsed`() =
        runTest {
            val watched = MutableStateFlow(Result.success(listOf(villeneuveMovie(1), nolanMovie(2))))
            every { getWatchedMoviesUseCase() } returns watched
            viewModel = loadedDirectorViewModel()
            viewModel.process(WatchedMoviesIntent.ToggleGroup(NOLAN_KEY))
            advanceUntilIdle()

            watched.value = Result.success(listOf(villeneuveMovie(1)))
            advanceUntilIdle()
            watched.value = Result.success(listOf(villeneuveMovie(1), nolanMovie(2)))
            advanceUntilIdle()

            viewModel.expandedKeys() shouldBeEqualTo emptyList()
        }

    @Test
    fun `GIVEN search with matches WHEN CloseSearch THEN directors return to their previous state`() =
        runTest {
            every { searchWatchedMoviesUseCase("dune") } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, title = "Dune"))))
            viewModel = loadedDirectorViewModel(villeneuveMovie(1, title = "Dune"), nolanMovie(2))
            viewModel.process(WatchedMoviesIntent.OpenSearch)
            viewModel.process(WatchedMoviesIntent.UpdateQuery("dune"))
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.CloseSearch)

            viewModel.uiState.value.groups
                .map { it.key to it.isExpanded }
                .toSet() shouldBeEqualTo
                setOf(VILLENEUVE_KEY to false, NOLAN_KEY to false)
        }

    @Test
    fun `GIVEN expanded director WHEN grouping changes away and back THEN it stays expanded`() =
        runTest {
            viewModel = loadedDirectorViewModel(villeneuveMovie(1), nolanMovie(2))
            viewModel.process(WatchedMoviesIntent.ToggleGroup(NOLAN_KEY))
            advanceUntilIdle()

            viewModel.process(WatchedMoviesIntent.ChangeGrouping(WatchedGrouping.DECADE))
            viewModel.process(WatchedMoviesIntent.ChangeGrouping(WatchedGrouping.DIRECTOR))
            advanceUntilIdle()

            viewModel.expandedKeys() shouldBeEqualTo listOf(NOLAN_KEY)
        }

    @Test
    fun `GIVEN groupings other than director WHEN loaded THEN groups are not collapsible and show movies`() =
        runTest {
            every { getWatchedMoviesUseCase() } returns
                flowOf(Result.success(listOf(villeneuveMovie(1, releaseDate = "2021-09-15"))))
            viewModel = createViewModel()
            viewModel.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()

            listOf(WatchedGrouping.ALPHABETICAL, WatchedGrouping.DECADE, WatchedGrouping.RECENT).forEach { grouping ->
                viewModel.process(WatchedMoviesIntent.ChangeGrouping(grouping))
                viewModel.uiState.value.groups
                    .map { it.isCollapsible to it.isExpanded } shouldBeEqualTo
                    listOf(false to true)
            }
        }

    private fun TestScope.loadedDirectorViewModel(vararg movies: Movie): WatchedMoviesViewModel {
        every { userPreferencesRepository.watchedGrouping } returns flowOf(WatchedGrouping.DIRECTOR)
        if (movies.isNotEmpty()) {
            every { getWatchedMoviesUseCase() } returns flowOf(Result.success(movies.toList()))
        }
        return createViewModel().also {
            it.process(WatchedMoviesIntent.LoadMovies)
            advanceUntilIdle()
        }
    }

    private fun WatchedMoviesViewModel.expandedKeys() =
        uiState.value.groups
            .filter { it.isExpanded }
            .map { it.key }

    private fun nolanMovie(id: Int) = buildMovie(id).copy(directors = listOf(Director(NOLAN_ID, "Christopher Nolan")))

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
    ) = Movie(
        id = id,
        title = title,
        overview = "",
        posterPath = null,
        backdropPath = null,
        releaseDate = null,
        popularity = 0.0,
        voteAverage = 0.0,
        voteCount = 0,
        genres = emptyList(),
        productionCountries = emptyList(),
        isWatched = true,
        isInWatchlist = false,
    )

    private companion object {
        const val VILLENEUVE_ID = 137427
        const val NOLAN_ID = 525
        const val VILLENEUVE_KEY = "director:137427"
        const val NOLAN_KEY = "director:525"
        const val UNKNOWN_DIRECTOR_KEY = "director:unknown"
    }
}
