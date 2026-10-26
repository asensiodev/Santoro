package com.asensiodev.feature.watchlist.impl.presentation

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.repository.MovieMutationRepository
import com.asensiodev.core.domain.repository.SyncScheduler
import com.asensiodev.feature.watchlist.impl.domain.usecase.GetWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.SearchWatchlistMoviesUseCase
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistSurpriseViewModelTest {
    private val getMovies: GetWatchlistMoviesUseCase = mockk()
    private val searchMovies: SearchWatchlistMoviesUseCase = mockk(relaxed = true)
    private val mutations: MovieMutationRepository = mockk(relaxed = true)
    private val sync: SyncScheduler = mockk(relaxed = true)
    private val movies = MutableStateFlow(Result.success(listOf(movie(1, "Arrival"))))
    private lateinit var viewModel: WatchlistMoviesViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        every { getMovies() } returns movies
        viewModel = WatchlistMoviesViewModel(getMovies, searchMovies, mutations, sync)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `single saved movie is suggested without changing the list`() =
        runTest {
            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()
            val savedMovies = viewModel.uiState.value.movies

            viewModel.process(WatchlistIntent.SuggestMovie)

            viewModel.uiState.value.suggestedMovie
                ?.title shouldBeEqualTo "Arrival"
            viewModel.uiState.value.movies shouldBeEqualTo savedMovies
        }

    @Test
    fun `another suggestion alternates two movies on rapid requests without writes`() =
        runTest {
            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchlistIntent.SuggestMovie)
            movies.value = Result.success(listOf(movie(1, "Arrival"), movie(2, "Inception")))
            advanceUntilIdle()

            repeat(10) {
                viewModel.process(WatchlistIntent.SuggestMovie)
                viewModel.uiState.value.suggestedMovie
                    ?.id shouldBeEqualTo 2
                viewModel.process(WatchlistIntent.SuggestMovie)
                viewModel.uiState.value.suggestedMovie
                    ?.id shouldBeEqualTo 1
            }

            confirmVerified(mutations, sync)
        }

    @Test
    fun `dismissing the suggestion preserves saved movies`() =
        runTest {
            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchlistIntent.SuggestMovie)

            viewModel.process(WatchlistIntent.DismissSuggestion)

            viewModel.uiState.value.suggestedMovie shouldBeEqualTo null
            viewModel.uiState.value.movies
                .map { it.title } shouldBeEqualTo listOf("Arrival")
        }

    @Test
    fun `entering a query immediately clears and blocks suggestions`() =
        runTest {
            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchlistIntent.SuggestMovie)

            viewModel.process(WatchlistIntent.UpdateQuery("arrival"))

            viewModel.uiState.value.suggestedMovie shouldBeEqualTo null
            viewModel.process(WatchlistIntent.SuggestMovie)
            viewModel.uiState.value.suggestedMovie shouldBeEqualTo null
        }

    @Test
    fun `failed content cannot suggest a stale saved movie`() =
        runTest {
            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()
            movies.value = Result.failure(IllegalStateException("Unavailable"))
            advanceUntilIdle()

            viewModel.process(WatchlistIntent.SuggestMovie)

            viewModel.uiState.value.suggestedMovie shouldBeEqualTo null
        }

    @Test
    fun `removing the suggested movie from the local stream closes the suggestion`() =
        runTest {
            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchlistIntent.SuggestMovie)

            movies.value = Result.success(listOf(movie(2, "Inception")))
            advanceUntilIdle()

            viewModel.uiState.value.suggestedMovie shouldBeEqualTo null
            viewModel.uiState.value.movies
                .map { it.title } shouldBeEqualTo listOf("Inception")
        }

    @Test
    fun `a local failure closes an open suggestion and prevents reopening it`() =
        runTest {
            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchlistIntent.SuggestMovie)

            movies.value = Result.failure(IllegalStateException("Unavailable"))
            advanceUntilIdle()

            viewModel.uiState.value.suggestedMovie shouldBeEqualTo null
            viewModel.process(WatchlistIntent.SuggestMovie)
            viewModel.uiState.value.suggestedMovie shouldBeEqualTo null
        }

    @Test
    fun `local metadata updates refresh the existing suggestion`() =
        runTest {
            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchlistIntent.SuggestMovie)

            movies.value = Result.success(listOf(movie(1, "La llegada")))
            advanceUntilIdle()

            viewModel.uiState.value.suggestedMovie
                ?.title shouldBeEqualTo "La llegada"
        }

    @Test
    fun `loading and empty collections cannot produce a suggestion`() =
        runTest {
            viewModel.process(WatchlistIntent.SuggestMovie)
            viewModel.uiState.value.suggestedMovie shouldBeEqualTo null

            movies.value = Result.success(emptyList())
            viewModel.process(WatchlistIntent.LoadMovies)
            advanceUntilIdle()
            viewModel.process(WatchlistIntent.SuggestMovie)

            viewModel.uiState.value.suggestedMovie shouldBeEqualTo null
        }

    private fun movie(
        id: Int,
        title: String,
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
        isWatched = false,
        isInWatchlist = true,
    )
}
