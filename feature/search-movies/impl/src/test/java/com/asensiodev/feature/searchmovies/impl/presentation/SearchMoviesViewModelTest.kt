package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.lifecycle.SavedStateHandle
import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetMoviesByGenreUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetNowPlayingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetPopularMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTopRatedMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTrendingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetUpcomingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesByQueryAndGenreUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SearchMoviesUseCase
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
    private val searchMoviesByQueryAndGenreUseCase: SearchMoviesByQueryAndGenreUseCase =
        mockk(relaxed = true)

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

        every { getNowPlayingMoviesUseCase(any()) } returns flowOf(Result.Success(emptyList()))
        every { getPopularMoviesUseCase(any()) } returns flowOf(Result.Success(emptyList()))
        every { getTopRatedMoviesUseCase(any()) } returns flowOf(Result.Success(emptyList()))
        every { getUpcomingMoviesUseCase(any()) } returns flowOf(Result.Success(emptyList()))
        every { getTrendingMoviesUseCase(any()) } returns flowOf(Result.Success(emptyList()))

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
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN casino search WHEN drama genre selected THEN casino movie should still appear`() =
        runTest {
            every { searchMoviesUseCase("casino", any()) } returns
                flowOf(Result.Success(listOf(casinoMovie)))

            every { searchMoviesByQueryAndGenreUseCase("casino", 18, any()) } returns
                flowOf(Result.Success(listOf(casinoMovie)))

            viewModel.loadInitialData()
            advanceUntilIdle()

            viewModel.updateQuery("casino")
            testDispatcher.scheduler.advanceTimeBy(600)
            advanceUntilIdle()

            val stateAfterQuery = viewModel.uiState.value
            stateAfterQuery.searchMovieResults.size shouldBeEqualTo 1
            stateAfterQuery.searchMovieResults.first().title shouldBeEqualTo "Casino"

            viewModel.onGenreSelected(18)
            advanceUntilIdle()

            val stateAfterGenre = viewModel.uiState.value
            stateAfterGenre.screenState shouldBeInstanceOf SearchScreenState.Content::class
            stateAfterGenre.searchMovieResults.size shouldBeEqualTo 1
            stateAfterGenre.searchMovieResults.first().title shouldBeEqualTo "Casino"
        }

    @Test
    fun `GIVEN casino search with drama genre WHEN genre cleared THEN casino should still appear`() =
        runTest {
            every { searchMoviesUseCase("casino", any()) } returns
                flowOf(Result.Success(listOf(casinoMovie)))

            every { searchMoviesByQueryAndGenreUseCase("casino", 18, any()) } returns
                flowOf(Result.Success(listOf(casinoMovie)))

            viewModel.loadInitialData()
            advanceUntilIdle()

            viewModel.updateQuery("casino")
            testDispatcher.scheduler.advanceTimeBy(600)
            advanceUntilIdle()

            viewModel.onGenreSelected(18)
            advanceUntilIdle()

            viewModel.clearGenreSelection()
            advanceUntilIdle()

            val finalState = viewModel.uiState.value
            finalState.screenState shouldBeInstanceOf SearchScreenState.Content::class
            finalState.searchMovieResults.size shouldBeEqualTo 1
            finalState.searchMovieResults.first().title shouldBeEqualTo "Casino"
        }
}
