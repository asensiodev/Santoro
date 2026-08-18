package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.lifecycle.SavedStateHandle
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.domain.model.MovieLibraryStatus
import com.asensiodev.feature.searchmovies.impl.domain.usecase.ClearRecentSearchesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetMoviesByGenreUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetNowPlayingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetPopularMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetRecentSearchesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTopRatedMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTrendingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetUpcomingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.ObserveMovieLibraryStatusesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SaveRecentSearchUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesByQueryAndGenreUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesUseCase
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchMoviesViewModelStatusTest {
    private val searchMoviesUseCase: SearchMoviesUseCase = mockk(relaxed = true)
    private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCase = mockk(relaxed = true)
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase = mockk(relaxed = true)
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase = mockk(relaxed = true)
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase = mockk(relaxed = true)
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase = mockk(relaxed = true)
    private val getMoviesByGenreUseCase: GetMoviesByGenreUseCase = mockk(relaxed = true)
    private val searchMoviesByQueryAndGenreUseCase: SearchMoviesByQueryAndGenreUseCase = mockk(relaxed = true)
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase = mockk(relaxed = true)
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase = mockk(relaxed = true)
    private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase = mockk(relaxed = true)
    private val observeMovieLibraryStatusesUseCase: ObserveMovieLibraryStatusesUseCase = mockk()
    private val libraryStatuses =
        MutableStateFlow<Result<Map<Int, MovieLibraryStatus>>>(Result.success(emptyMap()))
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SearchMoviesViewModel

    private val casinoMovie =
        Movie(
            id = 1,
            title = "Casino",
            posterPath = "/casino.jpg",
            backdropPath = null,
            overview = "Overview",
            releaseDate = "1995-11-22",
            popularity = 7.8,
            voteAverage = 8.2,
            voteCount = 5000,
            genres = listOf(Genre(18, "Drama")),
            genreIds = listOf(18),
            productionCountries = emptyList(),
            isWatched = false,
            isInWatchlist = false,
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getNowPlayingMoviesUseCase(any()) } returns flowOf(Result.success(emptyList()))
        every { getPopularMoviesUseCase(any()) } returns flowOf(Result.success(emptyList()))
        every { getTopRatedMoviesUseCase(any()) } returns flowOf(Result.success(emptyList()))
        every { getUpcomingMoviesUseCase(any()) } returns flowOf(Result.success(emptyList()))
        every { getTrendingMoviesUseCase(any()) } returns flowOf(Result.success(emptyList()))
        every { getRecentSearchesUseCase() } returns flowOf(emptyList())
        every { observeMovieLibraryStatusesUseCase() } returns libraryStatuses
        coJustRun { saveRecentSearchUseCase(any()) }
        coJustRun { clearRecentSearchesUseCase() }
        viewModel =
            SearchMoviesViewModel(
                savedStateHandle = SavedStateHandle(),
                searchMoviesUseCase = searchMoviesUseCase,
                getNowPlayingMoviesUseCase = getNowPlayingMoviesUseCase,
                getPopularMoviesUseCase = getPopularMoviesUseCase,
                getTopRatedMoviesUseCase = getTopRatedMoviesUseCase,
                getUpcomingMoviesUseCase = getUpcomingMoviesUseCase,
                getTrendingMoviesUseCase = getTrendingMoviesUseCase,
                getMoviesByGenreUseCase = getMoviesByGenreUseCase,
                searchMoviesByQueryAndGenreUseCase = searchMoviesByQueryAndGenreUseCase,
                getRecentSearchesUseCase = getRecentSearchesUseCase,
                saveRecentSearchUseCase = saveRecentSearchUseCase,
                clearRecentSearchesUseCase = clearRecentSearchesUseCase,
                observeMovieLibraryStatusesUseCase = observeMovieLibraryStatusesUseCase,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN loaded dashboard WHEN statuses change THEN every section updates reactively`() =
        runTest {
            every { getNowPlayingMoviesUseCase(1) } returns flowOf(Result.success(listOf(casinoMovie)))
            every { getPopularMoviesUseCase(1) } returns flowOf(Result.success(listOf(casinoMovie)))
            every { getTopRatedMoviesUseCase(1) } returns flowOf(Result.success(listOf(casinoMovie)))
            every { getUpcomingMoviesUseCase(1) } returns flowOf(Result.success(listOf(casinoMovie)))
            every { getTrendingMoviesUseCase(1) } returns flowOf(Result.success(listOf(casinoMovie)))

            loadInitial()
            libraryStatuses.emit(Result.success(mapOf(1 to MovieLibraryStatus.Watched)))
            runCurrent()

            val state = viewModel.uiState.value
            listOf(
                state.nowPlayingMovies,
                state.popularMovies,
                state.topRatedMovies,
                state.upcomingMovies,
                state.trendingMovies,
            ).forEach { movies ->
                movies.single().libraryStatus shouldBeEqualTo MovieLibraryStatus.Watched
            }
        }

    @Test
    fun `GIVEN filtered results WHEN statuses change THEN filtered content updates`() =
        runTest {
            every { searchMoviesByQueryAndGenreUseCase("casino", 18, 1) } returns
                flowOf(Result.success(listOf(casinoMovie)))

            loadInitial()
            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            testDispatcher.scheduler.advanceTimeBy(600)
            viewModel.process(SearchMoviesIntent.SelectGenre(18))
            advanceUntilIdle()
            libraryStatuses.emit(Result.success(mapOf(1 to MovieLibraryStatus.Watchlist)))
            runCurrent()

            val state = viewModel.uiState.value
            state.selectedGenreId shouldBeEqualTo 18
            state.searchMovieResults.single().libraryStatus shouldBeEqualTo
                MovieLibraryStatus.Watchlist
        }

    @Test
    fun `GIVEN paginated search WHEN statuses change and fail THEN content and session survive`() =
        runTest {
            val secondMovie = casinoMovie.copy(id = 2, title = "Casino Royale")
            every { searchMoviesUseCase("casino", 1) } returns flowOf(Result.success(listOf(casinoMovie)))
            every { searchMoviesUseCase("casino", 2) } returns flowOf(Result.success(listOf(secondMovie)))

            loadInitial()
            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            testDispatcher.scheduler.advanceTimeBy(600)
            advanceUntilIdle()
            viewModel.process(SearchMoviesIntent.LoadMoreSearchResults)
            advanceUntilIdle()
            libraryStatuses.emit(
                Result.success(
                    mapOf(
                        1 to MovieLibraryStatus.Watched,
                        2 to MovieLibraryStatus.Watchlist,
                    ),
                ),
            )
            runCurrent()

            val successfulState = viewModel.uiState.value
            successfulState.query shouldBeEqualTo "casino"
            successfulState.currentSearchPage shouldBeEqualTo 2
            successfulState.searchMovieResults.map { movie -> movie.libraryStatus } shouldBeEqualTo
                listOf(MovieLibraryStatus.Watched, MovieLibraryStatus.Watchlist)

            libraryStatuses.emit(Result.failure(IllegalStateException()))
            runCurrent()

            viewModel.uiState.value shouldBeEqualTo successfulState
            verify(exactly = 1) { searchMoviesUseCase("casino", 1) }
            verify(exactly = 1) { searchMoviesUseCase("casino", 2) }
        }

    @Test
    fun `GIVEN decorated results WHEN statuses clear THEN ribbons disappear without reload`() =
        runTest {
            every { searchMoviesUseCase("casino", 1) } returns flowOf(Result.success(listOf(casinoMovie)))

            loadInitial()
            viewModel.process(SearchMoviesIntent.UpdateQuery("casino"))
            testDispatcher.scheduler.advanceTimeBy(600)
            advanceUntilIdle()
            libraryStatuses.emit(Result.success(mapOf(1 to MovieLibraryStatus.Watched)))
            runCurrent()
            libraryStatuses.emit(Result.success(emptyMap()))
            runCurrent()

            viewModel.uiState.value.searchMovieResults
                .single()
                .libraryStatus shouldBeEqualTo null
            verify(exactly = 1) { searchMoviesUseCase("casino", 1) }
        }

    private suspend fun TestScope.loadInitial() {
        viewModel.process(SearchMoviesIntent.LoadInitialData)
        advanceUntilIdle()
    }
}
