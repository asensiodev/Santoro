package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.data.repository.CachingSearchMoviesRepository
import com.asensiodev.feature.searchmovies.impl.data.repository.StaleDataException
import com.asensiodev.feature.searchmovies.impl.domain.usecase.ClearRecentSearchesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetMoviesByGenreUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetNowPlayingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetPopularMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetRecentSearchesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTopRatedMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTrendingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetUpcomingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SaveRecentSearchUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesByQueryAndGenreUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesUseCase
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchMoviesViewModelTest {
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
    private val searchMoviesUseCase: SearchMoviesUseCase = mockk(relaxed = true)
    private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCase = mockk(relaxed = true)
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase = mockk(relaxed = true)
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase = mockk(relaxed = true)
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase = mockk(relaxed = true)
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase = mockk(relaxed = true)
    private val getMoviesByGenreUseCase: GetMoviesByGenreUseCase = mockk(relaxed = true)
    private val searchMoviesByQueryAndGenreUseCase: SearchMoviesByQueryAndGenreUseCase = mockk(relaxed = true)
    private val cachingRepository: CachingSearchMoviesRepository = mockk(relaxed = true)
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase = mockk(relaxed = true)
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase = mockk(relaxed = true)
    private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase = mockk(relaxed = true)

    private lateinit var viewModel: SearchMoviesViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val casinoMovie =
        Movie(
            id = 1,
            title = "Casino",
            posterPath = "/casino.jpg",
            backdropPath = null,
            overview = "A tale of greed, deception, money, power.",
            releaseDate = "1995-11-22",
            popularity = 7.8,
            voteAverage = 8.2,
            voteCount = 5000,
            genres = listOf(Genre(18, "Drama")),
            genreIds = listOf(18),
            productionCountries = listOf(),
            isWatched = false,
            isInWatchlist = false,
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coJustRun { cachingRepository.clearStaleEntries() }
        every { getNowPlayingMoviesUseCase(any()) } returns flowOf(Result.success(emptyList()))
        every { getPopularMoviesUseCase(any()) } returns flowOf(Result.success(emptyList()))
        every { getTopRatedMoviesUseCase(any()) } returns flowOf(Result.success(emptyList()))
        every { getUpcomingMoviesUseCase(any()) } returns flowOf(Result.success(emptyList()))
        every { getTrendingMoviesUseCase(any()) } returns flowOf(Result.success(emptyList()))
        every { getRecentSearchesUseCase() } returns flowOf(emptyList())
        coJustRun { saveRecentSearchUseCase(any()) }
        coJustRun { clearRecentSearchesUseCase() }

        createViewModel()
    }

    private fun createViewModel(handle: SavedStateHandle = savedStateHandle) {
        viewModel =
            SearchMoviesViewModel(
                savedStateHandle = handle,
                searchMoviesUseCase = searchMoviesUseCase,
                getNowPlayingMoviesUseCase = getNowPlayingMoviesUseCase,
                getPopularMoviesUseCase = getPopularMoviesUseCase,
                getTopRatedMoviesUseCase = getTopRatedMoviesUseCase,
                getUpcomingMoviesUseCase = getUpcomingMoviesUseCase,
                getTrendingMoviesUseCase = getTrendingMoviesUseCase,
                getMoviesByGenreUseCase = getMoviesByGenreUseCase,
                searchMoviesByQueryAndGenreUseCase = searchMoviesByQueryAndGenreUseCase,
                cachingRepository = cachingRepository,
                getRecentSearchesUseCase = getRecentSearchesUseCase,
                saveRecentSearchUseCase = saveRecentSearchUseCase,
                clearRecentSearchesUseCase = clearRecentSearchesUseCase,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN casino search WHEN drama genre selected THEN casino movie should still appear`() =
        runTest {
            every { searchMoviesUseCase("casino", any()) } returns flowOf(Result.success(listOf(casinoMovie)))
            every { searchMoviesByQueryAndGenreUseCase("casino", 18, any()) } returns
                flowOf(Result.success(listOf(casinoMovie)))

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            testDispatcher.scheduler.advanceTimeBy(600)
            advanceUntilIdle()

            val stateAfterQuery = viewModel.uiState.value
            stateAfterQuery.searchMovieResults.size shouldBeEqualTo 1
            stateAfterQuery.searchMovieResults.first().title shouldBeEqualTo "Casino"

            viewModel.process(SearchMoviesIntent.SelectGenre(18))
            advanceUntilIdle()

            val stateAfterGenre = viewModel.uiState.value
            stateAfterGenre.screenState shouldBeInstanceOf SearchScreenState.Content::class
            stateAfterGenre.searchMovieResults.size shouldBeEqualTo 1
            stateAfterGenre.searchMovieResults.first().title shouldBeEqualTo "Casino"
        }

    @Test
    fun `GIVEN casino search with drama genre WHEN genre cleared THEN casino should still appear`() =
        runTest {
            every { searchMoviesUseCase("casino", any()) } returns flowOf(Result.success(listOf(casinoMovie)))
            every { searchMoviesByQueryAndGenreUseCase("casino", 18, any()) } returns
                flowOf(Result.success(listOf(casinoMovie)))

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            testDispatcher.scheduler.advanceTimeBy(600)
            advanceUntilIdle()

            viewModel.process(SearchMoviesIntent.SelectGenre(18))
            advanceUntilIdle()

            viewModel.process(SearchMoviesIntent.ClearGenre)
            advanceUntilIdle()

            val finalState = viewModel.uiState.value
            finalState.screenState shouldBeInstanceOf SearchScreenState.Content::class
            finalState.searchMovieResults.size shouldBeEqualTo 1
            finalState.searchMovieResults.first().title shouldBeEqualTo "Casino"
        }

    @Test
    fun `GIVEN StaleDataException WHEN loadInitialData THEN isShowingStaleData true and screenState Content`() =
        runTest {
            every { getPopularMoviesUseCase(any()) } returns
                flow {
                    emit(Result.success(listOf(casinoMovie)))
                    emit(Result.failure(StaleDataException()))
                }

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.isShowingStaleData shouldBeEqualTo true
            state.screenState shouldBeInstanceOf SearchScreenState.Content::class
        }

    @Test
    fun `GIVEN fresh result WHEN loadInitialData THEN isShowingStaleData is false`() =
        runTest {
            every { getPopularMoviesUseCase(any()) } returns flowOf(Result.success(listOf(casinoMovie)))

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.uiState.value.isShowingStaleData shouldBeEqualTo false
        }

    @Test
    fun `GIVEN dashboard content WHEN refresh fails THEN keeps content and shows stale banner`() =
        runTest {
            var shouldFail = false
            every { getPopularMoviesUseCase(any()) } answers {
                if (shouldFail) {
                    flowOf(Result.failure(java.io.IOException("Unable to resolve host")))
                } else {
                    flowOf(Result.success(listOf(casinoMovie)))
                }
            }

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            shouldFail = true
            viewModel.process(SearchMoviesIntent.Refresh)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.screenState shouldBeInstanceOf SearchScreenState.Content::class
            state.popularMovies.size shouldBeEqualTo 1
            state.popularMovies.first().title shouldBeEqualTo "Casino"
            state.isShowingStaleData shouldBeEqualTo true
            state.isRefreshing shouldBeEqualTo false
            coVerify(exactly = 0) { cachingRepository.clearAllSections() }
        }

    @Test
    fun `GIVEN dashboard content WHEN refresh throws THEN stops refreshing and keeps content`() =
        runTest {
            var shouldThrow = false
            every { getPopularMoviesUseCase(any()) } answers {
                if (shouldThrow) {
                    flow { throw java.io.IOException("Unable to resolve host") }
                } else {
                    flowOf(Result.success(listOf(casinoMovie)))
                }
            }

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            shouldThrow = true
            viewModel.process(SearchMoviesIntent.Refresh)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.screenState shouldBeInstanceOf SearchScreenState.Content::class
            state.popularMovies.size shouldBeEqualTo 1
            state.popularMovies.first().title shouldBeEqualTo "Casino"
            state.isShowingStaleData shouldBeEqualTo true
            state.isRefreshing shouldBeEqualTo false
        }

    @Test
    fun `GIVEN empty browse content WHEN query updated THEN loading is shown before debounced search`() =
        runTest {
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.process(SearchMoviesIntent.UpdateQuery("c"))

            viewModel.uiState.value.screenState shouldBeInstanceOf SearchScreenState.Loading::class
            viewModel.uiState.value.searchMovieResults shouldBeEqualTo emptyList()
        }

    @Test
    fun `GIVEN network error with no cache WHEN loadInitialData THEN isShowingStaleData is false`() =
        runTest {
            every { getPopularMoviesUseCase(any()) } returns
                flowOf(Result.failure(java.io.IOException("Network error")))

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.uiState.value.isShowingStaleData shouldBeEqualTo false
        }

    @Test
    fun `GIVEN dashboard flow throws cancellation WHEN loading THEN no error stale or default state is created`() =
        runTest {
            every {
                getPopularMoviesUseCase(any())
            } returns flow { throw CancellationException() }

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.screenState shouldBeInstanceOf SearchScreenState.Loading::class
            state.isShowingStaleData shouldBeEqualTo false
            state.popularMovies shouldBeEqualTo emptyList()
        }

    @Test
    fun `GIVEN search flow throws cancellation WHEN searching THEN error state is not created`() =
        runTest {
            every {
                searchMoviesUseCase("casino", 1)
            } returns flow { throw CancellationException() }

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()
            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            testDispatcher.scheduler.advanceTimeBy(600)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.screenState shouldBeInstanceOf SearchScreenState.Loading::class
            state.isShowingStaleData shouldBeEqualTo false
            state.searchMovieResults shouldBeEqualTo emptyList()
        }

    @Test
    fun `GIVEN popular page flow throws cancellation WHEN loading more THEN content is preserved`() =
        runTest {
            every {
                getPopularMoviesUseCase(1)
            } returns flowOf(Result.success(listOf(casinoMovie)))

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            every {
                getPopularMoviesUseCase(2)
            } returns flow { throw CancellationException() }
            viewModel.process(SearchMoviesIntent.LoadMorePopularMovies)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.screenState shouldBeInstanceOf SearchScreenState.Content::class
            state.popularMovies.map { movie -> movie.title } shouldBeEqualTo listOf("Casino")
            state.isShowingStaleData shouldBeEqualTo false
            state.isPopularEndReached shouldBeEqualTo false
        }

    @Test
    fun `GIVEN field not focused WHEN FieldFocused intent THEN isFieldFocused is true`() =
        runTest {
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.process(SearchMoviesIntent.FieldFocused)
            advanceUntilIdle()

            viewModel.uiState.value.isFieldFocused shouldBeEqualTo true
        }

    @Test
    fun `GIVEN field focused WHEN FieldCleared intent THEN isFieldFocused is false`() =
        runTest {
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()
            viewModel.process(SearchMoviesIntent.FieldFocused)

            viewModel.process(SearchMoviesIntent.FieldCleared)
            advanceUntilIdle()

            viewModel.uiState.value.isFieldFocused shouldBeEqualTo false
        }

    @Test
    fun `GIVEN empty query WHEN SuggestionTapped with avatar THEN query becomes avatar`() =
        runTest {
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.process(SearchMoviesIntent.SuggestionTapped("avatar"))
            advanceUntilIdle()

            viewModel.uiState.value.query shouldBeEqualTo "avatar"
            viewModel.uiState.value.isFieldFocused shouldBeEqualTo false
        }

    @Test
    fun `GIVEN recent searches present WHEN ClearRecentSearches intent THEN recentSearches is empty`() =
        runTest {
            every { getRecentSearchesUseCase() } returns flowOf(listOf("inception", "avatar"))
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            every { getRecentSearchesUseCase() } returns flowOf(emptyList())

            viewModel.process(SearchMoviesIntent.ClearRecentSearches)
            advanceUntilIdle()

            coVerify(exactly = 1) { clearRecentSearchesUseCase() }
        }

    @Test
    fun `GIVEN active query WHEN SearchTriggered THEN saves query`() =
        runTest {
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()
            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            advanceUntilIdle()

            viewModel.process(SearchMoviesIntent.SearchTriggered)
            advanceUntilIdle()

            coVerify(exactly = 1) { saveRecentSearchUseCase("casino") }
        }

    @Test
    fun `GIVEN blank query WHEN SearchTriggered THEN does not save`() =
        runTest {
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.process(SearchMoviesIntent.SearchTriggered)
            advanceUntilIdle()

            coVerify(exactly = 0) { saveRecentSearchUseCase(any()) }
        }

    @Test
    fun `GIVEN active query WHEN MovieClicked THEN saves query and emits NavigateToDetail`() =
        runTest {
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()
            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            advanceUntilIdle()

            viewModel.process(SearchMoviesIntent.MovieClicked(42))
            advanceUntilIdle()

            coVerify(exactly = 1) { saveRecentSearchUseCase("casino") }
        }

    @Test
    fun `GIVEN blank query WHEN MovieClicked THEN does not save but still navigates`() =
        runTest {
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.process(SearchMoviesIntent.MovieClicked(42))
            advanceUntilIdle()

            coVerify(exactly = 0) { saveRecentSearchUseCase(any()) }
        }

    @Test
    fun `GIVEN browse screen WHEN SeeAllClicked POPULAR THEN emits NavigateToSeeAll POPULAR effect`() =
        runTest {
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.effect.test {
                viewModel.process(
                    SearchMoviesIntent.SeeAllClicked(
                        com.asensiodev.feature.searchmovies.impl.presentation.model.SectionType.POPULAR,
                    ),
                )

                val effect = awaitItem()
                effect shouldBeInstanceOf SearchMoviesEffect.NavigateToSeeAll::class
                (effect as SearchMoviesEffect.NavigateToSeeAll).sectionType shouldBeEqualTo
                    com.asensiodev.feature.searchmovies.impl.presentation.model.SectionType.POPULAR
            }
        }

    @Test
    fun `GIVEN all dashboard calls fail without cache WHEN loadInitialData THEN screenState is Error`() =
        runTest {
            every { getPopularMoviesUseCase(any()) } returns flowOf(Result.failure(java.io.IOException("No network")))
            every { getNowPlayingMoviesUseCase(any()) } returns
                flowOf(Result.failure(java.io.IOException("No network")))
            every { getTopRatedMoviesUseCase(any()) } returns flowOf(Result.failure(java.io.IOException("No network")))
            every { getUpcomingMoviesUseCase(any()) } returns flowOf(Result.failure(java.io.IOException("No network")))
            every { getTrendingMoviesUseCase(any()) } returns flowOf(Result.failure(java.io.IOException("No network")))

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.uiState.value.screenState shouldBeInstanceOf SearchScreenState.Error::class
        }

    @Test
    fun `GIVEN restored query WHEN ViewModel is created THEN query is initialized from saved state`() =
        runTest {
            createViewModel(SavedStateHandle(mapOf("search_query" to "casino")))

            viewModel.uiState.value.query shouldBeEqualTo "casino"
        }

    @Test
    fun `GIVEN restored query WHEN initialized THEN searches restored query without loading dashboard`() =
        runTest {
            every { searchMoviesUseCase("casino", 1) } returns
                flowOf(Result.success(listOf(casinoMovie)))
            createViewModel(SavedStateHandle(mapOf("search_query" to "casino")))

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            verify(exactly = 1) { searchMoviesUseCase("casino", 1) }
            verify(exactly = 0) { getNowPlayingMoviesUseCase(any()) }
            verify(exactly = 0) { getPopularMoviesUseCase(any()) }
            viewModel.uiState.value.searchMovieResults
                .first()
                .title shouldBeEqualTo "Casino"
        }

    @Test
    fun `GIVEN initialized ViewModel WHEN LoadInitialData repeats THEN dashboard loads once`() =
        runTest {
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            verify(exactly = 1) { getPopularMoviesUseCase(1) }
            coVerify(exactly = 1) { cachingRepository.clearStaleEntries() }
        }

    @Test
    fun `GIVEN initialization just started WHEN query changes THEN first query is observed`() =
        runTest {
            every { searchMoviesUseCase("casino", 1) } returns
                flowOf(Result.success(listOf(casinoMovie)))

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            testDispatcher.scheduler.advanceTimeBy(600)
            advanceUntilIdle()

            verify(exactly = 1) { searchMoviesUseCase("casino", 1) }
            viewModel.uiState.value.searchMovieResults
                .map { movie -> movie.title } shouldBeEqualTo
                listOf("Casino")
        }

    @Test
    fun `GIVEN dashboard is loading WHEN query search starts THEN dashboard cannot overwrite search`() =
        runTest {
            every { getPopularMoviesUseCase(1) } returns flow { awaitCancellation() }
            every { searchMoviesUseCase("casino", 1) } returns
                flowOf(Result.success(listOf(casinoMovie)))

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            runCurrent()
            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            testDispatcher.scheduler.advanceTimeBy(600)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.query shouldBeEqualTo "casino"
            state.searchMovieResults.map { movie -> movie.title } shouldBeEqualTo listOf("Casino")
            state.popularMovies shouldBeEqualTo emptyList()
        }

    @Test
    fun `GIVEN query A is active WHEN query B starts THEN only query B can publish results`() =
        runTest {
            val queryAResults = MutableSharedFlow<Result<List<Movie>>>(extraBufferCapacity = 1)
            val queryBMovie = casinoMovie.copy(id = 2, title = "Casino Royale")
            every { searchMoviesUseCase("casino", 1) } returns queryAResults
            every { searchMoviesUseCase("royale", 1) } returns
                flowOf(Result.success(listOf(queryBMovie)))

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()
            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            testDispatcher.scheduler.advanceTimeBy(600)
            runCurrent()

            viewModel.process(SearchMoviesIntent.UpdateQuery("royale"))
            testDispatcher.scheduler.advanceTimeBy(600)
            advanceUntilIdle()
            queryAResults.emit(Result.success(listOf(casinoMovie)))
            advanceUntilIdle()

            viewModel.uiState.value.query shouldBeEqualTo "royale"
            viewModel.uiState.value.searchMovieResults
                .map { movie -> movie.title } shouldBeEqualTo
                listOf("Casino Royale")
        }

    @Test
    fun `GIVEN no active collector WHEN navigation effect is emitted THEN it is not replayed`() =
        runTest {
            viewModel.process(SearchMoviesIntent.MovieClicked(42))
            advanceUntilIdle()

            viewModel.effect.test {
                expectNoEvents()
            }
        }

    @Test
    fun `GIVEN active collector WHEN navigation effect is emitted THEN it is delivered`() =
        runTest {
            viewModel.effect.test {
                viewModel.process(SearchMoviesIntent.MovieClicked(42))
                runCurrent()

                awaitItem() shouldBeEqualTo SearchMoviesEffect.NavigateToDetail(42)
            }
        }
}
