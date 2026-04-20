package com.asensiodev.feature.watchlist.impl.presentation

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.coVerifyOnce
import com.asensiodev.feature.watchlist.impl.domain.usecase.GetWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.RemoveFromWatchlistUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.SearchWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistMoviesViewModelTest {
    private val getWatchlistMoviesUseCase: GetWatchlistMoviesUseCase = mockk(relaxed = true)
    private val searchWatchlistMoviesUseCase: SearchWatchlistMoviesUseCase = mockk(relaxed = true)
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase = mockk(relaxed = true)
    private val syncScheduler: WorkManagerSyncScheduler = mockk(relaxed = true)

    private lateinit var viewModel: WatchlistMoviesViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val inceptionMovieUi =
        MovieUi(
            id = 1,
            title = "Inception",
            posterPath = "https://image.tmdb.org/t/p/w500/inception.jpg",
            releaseYear = "2010",
            genres = "",
            rating = 8.8,
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getWatchlistMoviesUseCase() } returns flowOf(Result.success(emptyList()))
        viewModel =
            WatchlistMoviesViewModel(
                getWatchlistMoviesUseCase = getWatchlistMoviesUseCase,
                searchWatchlistMoviesUseCase = searchWatchlistMoviesUseCase,
                removeFromWatchlistUseCase = removeFromWatchlistUseCase,
                syncScheduler = syncScheduler,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a movie WHEN RequestRemove intent THEN movieToRemove is set in state`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.RequestRemove(inceptionMovieUi))

            viewModel.uiState.value.movieToRemove shouldBeEqualTo inceptionMovieUi
        }

    @Test
    fun `GIVEN movieToRemove set WHEN DismissRemoveDialog intent THEN movieToRemove is cleared`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.RequestRemove(inceptionMovieUi))
            viewModel.process(WatchlistIntent.DismissRemoveDialog)

            viewModel.uiState.value.movieToRemove
                .shouldBeNull()
        }

    @Test
    fun `GIVEN movieToRemove set WHEN ConfirmRemove intent THEN calls use case and clears movieToRemove`() =
        runTest {
            coEvery { removeFromWatchlistUseCase(inceptionMovieUi.id) } returns Result.success(true)
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.RequestRemove(inceptionMovieUi))
            viewModel.process(WatchlistIntent.ConfirmRemove)
            advanceUntilIdle()

            viewModel.uiState.value.movieToRemove
                .shouldBeNull()
            coVerifyOnce { removeFromWatchlistUseCase(inceptionMovieUi.id) }
        }

    @Test
    fun `GIVEN no movieToRemove WHEN ConfirmRemove intent THEN use case is never called`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.ConfirmRemove)
            advanceUntilIdle()

            viewModel.uiState.value.movieToRemove
                .shouldBeNull()
        }

    @Test
    fun `GIVEN movieToRemove set WHEN ConfirmRemove intent THEN enqueues upload for that movie`() =
        runTest {
            coEvery { removeFromWatchlistUseCase(inceptionMovieUi.id) } returns Result.success(true)
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.RequestRemove(inceptionMovieUi))
            viewModel.process(WatchlistIntent.ConfirmRemove)
            advanceUntilIdle()

            coVerify(exactly = 1) { syncScheduler.enqueueUpload(inceptionMovieUi.id) }
        }

    @Test
    fun `GIVEN remove fails WHEN ConfirmRemove intent THEN does not enqueue upload`() =
        runTest {
            coEvery {
                removeFromWatchlistUseCase(inceptionMovieUi.id)
            } returns Result.failure(Exception("db error"))
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.RequestRemove(inceptionMovieUi))
            viewModel.process(WatchlistIntent.ConfirmRemove)
            advanceUntilIdle()

            coVerify(exactly = 0) { syncScheduler.enqueueUpload(any()) }
        }

    @Test
    fun `GIVEN multiple movies WHEN ConfirmRemove intent THEN removes exactly one movie with correct id`() =
        runTest {
            val otherMovie =
                MovieUi(
                    id = 99,
                    title = "Other Movie",
                    posterPath = null,
                    releaseYear = "2020",
                    genres = null,
                    rating = 7.0,
                )
            coEvery { removeFromWatchlistUseCase(inceptionMovieUi.id) } returns Result.success(true)
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.RequestRemove(inceptionMovieUi))
            viewModel.process(WatchlistIntent.ConfirmRemove)
            advanceUntilIdle()

            coVerifyOnce { removeFromWatchlistUseCase(inceptionMovieUi.id) }
            coVerify(exactly = 0) { removeFromWatchlistUseCase(otherMovie.id) }
        }

    @Test
    fun `GIVEN no pending remove WHEN ConfirmRemove intent THEN use case is never called`() =
        runTest {
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.ConfirmRemove)
            advanceUntilIdle()

            coVerify(exactly = 0) { syncScheduler.enqueueUpload(any()) }
        }

    @Test
    fun `GIVEN empty watchlist WHEN LoadMovies intent THEN screenState becomes Empty`() =
        runTest {
            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.uiState.value.screenState shouldBeEqualTo WatchlistScreenState.Empty
            viewModel.uiState.value.listHeader
                .shouldBeNull()
            viewModel.uiState.value.totalMoviesCount shouldBeEqualTo 0
        }

    @Test
    fun `GIVEN watchlist has movies WHEN LoadMovies intent THEN totalMoviesCount matches full list`() =
        runTest {
            val movies = listOf(buildMovie(id = 1), buildMovie(id = 2), buildMovie(id = 3))
            every { getWatchlistMoviesUseCase() } returns flowOf(Result.success(movies))
            viewModel =
                WatchlistMoviesViewModel(
                    getWatchlistMoviesUseCase = getWatchlistMoviesUseCase,
                    searchWatchlistMoviesUseCase = searchWatchlistMoviesUseCase,
                    removeFromWatchlistUseCase = removeFromWatchlistUseCase,
                    syncScheduler = syncScheduler,
                )

            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()

            viewModel.uiState.value.listHeader shouldBeEqualTo WatchlistListHeaderUi.MoviesToWatch(3)
            viewModel.uiState.value.totalMoviesCount shouldBeEqualTo 3
        }

    @Test
    fun `GIVEN watchlist has movies WHEN search returns empty THEN screenState becomes NoResults`() =
        runTest {
            val movie = buildMovie(id = 1, title = "Inception")
            every { getWatchlistMoviesUseCase() } returns flowOf(Result.success(listOf(movie)))
            every { searchWatchlistMoviesUseCase("matrix") } returns flowOf(Result.success(emptyList()))
            viewModel =
                WatchlistMoviesViewModel(
                    getWatchlistMoviesUseCase = getWatchlistMoviesUseCase,
                    searchWatchlistMoviesUseCase = searchWatchlistMoviesUseCase,
                    removeFromWatchlistUseCase = removeFromWatchlistUseCase,
                    syncScheduler = syncScheduler,
                )

            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchlistIntent.UpdateQuery("matrix"))
            advanceTimeBy(500)
            advanceUntilIdle()

            viewModel.uiState.value.listHeader
                .shouldBeNull()
            viewModel.uiState.value.screenState shouldBeEqualTo WatchlistScreenState.NoResults
            viewModel.uiState.value.totalMoviesCount shouldBeEqualTo 1
        }

    @Test
    fun `GIVEN watchlist has movies WHEN search returns matches THEN header shows search results count`() =
        runTest {
            val inception = buildMovie(id = 1, title = "Inception")
            val interstellar = buildMovie(id = 2, title = "Interstellar")
            every {
                getWatchlistMoviesUseCase()
            } returns flowOf(Result.success(listOf(inception, interstellar)))
            every {
                searchWatchlistMoviesUseCase("in")
            } returns flowOf(Result.success(listOf(inception)))
            viewModel =
                WatchlistMoviesViewModel(
                    getWatchlistMoviesUseCase = getWatchlistMoviesUseCase,
                    searchWatchlistMoviesUseCase = searchWatchlistMoviesUseCase,
                    removeFromWatchlistUseCase = removeFromWatchlistUseCase,
                    syncScheduler = syncScheduler,
                )

            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchlistIntent.UpdateQuery("in"))
            advanceTimeBy(500)
            advanceUntilIdle()

            viewModel.uiState.value.screenState shouldBeEqualTo WatchlistScreenState.Content
            viewModel.uiState.value.listHeader shouldBeEqualTo WatchlistListHeaderUi.SearchResults(1)
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
            isWatched = false,
            isInWatchlist = true,
            watchedAt = null,
        )
}
