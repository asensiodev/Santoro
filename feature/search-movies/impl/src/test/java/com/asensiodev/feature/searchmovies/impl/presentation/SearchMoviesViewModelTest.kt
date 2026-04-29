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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
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

        viewModel =
            SearchMoviesViewModel(
                savedStateHandle = savedStateHandle,
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
    fun `GIVEN network error with no cache WHEN loadInitialData THEN isShowingStaleData is false`() =
        runTest {
            every { getPopularMoviesUseCase(any()) } returns
                flowOf(Result.failure(java.io.IOException("Network error")))

            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            viewModel.uiState.value.isShowingStaleData shouldBeEqualTo false
        }

    @Test
    fun `GIVEN field not focused WHEN FieldFocused intent THEN isFieldFocused is true`() =
        runTest {
            // GIVEN
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            // WHEN
            viewModel.process(SearchMoviesIntent.FieldFocused)
            advanceUntilIdle()

            // THEN
            viewModel.uiState.value.isFieldFocused shouldBeEqualTo true
        }

    @Test
    fun `GIVEN field focused WHEN FieldCleared intent THEN isFieldFocused is false`() =
        runTest {
            // GIVEN
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()
            viewModel.process(SearchMoviesIntent.FieldFocused)

            // WHEN
            viewModel.process(SearchMoviesIntent.FieldCleared)
            advanceUntilIdle()

            // THEN
            viewModel.uiState.value.isFieldFocused shouldBeEqualTo false
        }

    @Test
    fun `GIVEN empty query WHEN SuggestionTapped with avatar THEN query becomes avatar`() =
        runTest {
            // GIVEN
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            // WHEN
            viewModel.process(SearchMoviesIntent.SuggestionTapped("avatar"))
            advanceUntilIdle()

            // THEN
            viewModel.uiState.value.query shouldBeEqualTo "avatar"
            viewModel.uiState.value.isFieldFocused shouldBeEqualTo false
        }

    @Test
    fun `GIVEN recent searches present WHEN ClearRecentSearches intent THEN recentSearches is empty`() =
        runTest {
            // GIVEN
            every { getRecentSearchesUseCase() } returns flowOf(listOf("inception", "avatar"))
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            every { getRecentSearchesUseCase() } returns flowOf(emptyList())

            // WHEN
            viewModel.process(SearchMoviesIntent.ClearRecentSearches)
            advanceUntilIdle()

            // THEN
            coVerify(exactly = 1) { clearRecentSearchesUseCase() }
        }

    @Test
    fun `GIVEN active query WHEN SearchTriggered THEN saves query`() =
        runTest {
            // GIVEN
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()
            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            advanceUntilIdle()

            // WHEN
            viewModel.process(SearchMoviesIntent.SearchTriggered)
            advanceUntilIdle()

            // THEN
            coVerify(exactly = 1) { saveRecentSearchUseCase("casino") }
        }

    @Test
    fun `GIVEN blank query WHEN SearchTriggered THEN does not save`() =
        runTest {
            // GIVEN
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            // WHEN
            viewModel.process(SearchMoviesIntent.SearchTriggered)
            advanceUntilIdle()

            // THEN
            coVerify(exactly = 0) { saveRecentSearchUseCase(any()) }
        }

    @Test
    fun `GIVEN active query WHEN MovieClicked THEN saves query and emits NavigateToDetail`() =
        runTest {
            // GIVEN
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()
            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            advanceUntilIdle()

            // WHEN
            viewModel.process(SearchMoviesIntent.MovieClicked(42))
            advanceUntilIdle()

            // THEN
            coVerify(exactly = 1) { saveRecentSearchUseCase("casino") }
        }

    @Test
    fun `GIVEN blank query WHEN MovieClicked THEN does not save but still navigates`() =
        runTest {
            // GIVEN
            viewModel.process(SearchMoviesIntent.LoadInitialData)
            advanceUntilIdle()

            // WHEN
            viewModel.process(SearchMoviesIntent.MovieClicked(42))
            advanceUntilIdle()

            // THEN
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
}
