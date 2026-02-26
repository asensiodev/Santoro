package com.asensiodev.feature.watchedmovies.impl.presentation

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
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
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchedMoviesViewModelTest {
    private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase = mockk(relaxed = true)
    private val searchWatchedMoviesUseCase: SearchWatchedMoviesUseCase = mockk(relaxed = true)

    private lateinit var viewModel: WatchedMoviesViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getWatchedMoviesUseCase() } returns flowOf(Result.Success(emptyList()))
        viewModel =
            WatchedMoviesViewModel(
                getWatchedMoviesUseCase = getWatchedMoviesUseCase,
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
            val movie =
                Movie(
                    id = 1,
                    title = "Inception",
                    overview = "",
                    posterPath = null,
                    backdropPath = null,
                    releaseDate = null,
                    popularity = 0.0,
                    voteAverage = 0.0,
                    voteCount = 0,
                    genres = emptyList(),
                    productionCountries = emptyList(),
                    cast = emptyList(),
                    crew = emptyList(),
                    runtime = null,
                    director = null,
                    isWatched = true,
                    isInWatchlist = false,
                    watchedAt = null,
                )
            every { getWatchedMoviesUseCase() } returns flowOf(Result.Success(listOf(movie)))
            viewModel =
                WatchedMoviesViewModel(
                    getWatchedMoviesUseCase = getWatchedMoviesUseCase,
                    searchWatchedMoviesUseCase = searchWatchedMoviesUseCase,
                )

            advanceUntilIdle()

            viewModel.uiState.value.movies
                .shouldNotBeNull()
        }
}
