package com.asensiodev.feature.searchmovies.impl.presentation.seeall

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.data.repository.StaleDataException
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetPopularMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTopRatedMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTrendingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetUpcomingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.presentation.model.SectionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class SeeAllMoviesViewModelTest {
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase = mockk(relaxed = true)
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase = mockk(relaxed = true)
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase = mockk(relaxed = true)
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SeeAllMoviesViewModel

    private val sampleMovie =
        Movie(
            id = 1,
            title = "Inception",
            posterPath = "/inception.jpg",
            backdropPath = null,
            overview = "A mind-bending thriller",
            releaseDate = "2010-07-16",
            popularity = 9.0,
            voteAverage = 8.8,
            voteCount = 30000,
            genres = emptyList(),
            genreIds = emptyList(),
            productionCountries = emptyList(),
            isWatched = false,
            isInWatchlist = false,
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(sectionType: SectionType = SectionType.TRENDING) {
        val savedStateHandle =
            SavedStateHandle(mapOf("sectionType" to sectionType.key))
        viewModel =
            SeeAllMoviesViewModel(
                savedStateHandle = savedStateHandle,
                getTrendingMoviesUseCase = getTrendingMoviesUseCase,
                getPopularMoviesUseCase = getPopularMoviesUseCase,
                getTopRatedMoviesUseCase = getTopRatedMoviesUseCase,
                getUpcomingMoviesUseCase = getUpcomingMoviesUseCase,
            )
    }

    @Test
    fun `GIVEN trending section WHEN LoadInitial THEN calls GetTrendingMoviesUseCase with page 1`() =
        runTest {
            every { getTrendingMoviesUseCase(1) } returns flowOf(Result.success(listOf(sampleMovie)))
            createViewModel(SectionType.TRENDING)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()

            verify(exactly = 1) { getTrendingMoviesUseCase(1) }
        }

    @Test
    fun `GIVEN trending section WHEN LoadInitial success THEN state is Content with movies`() =
        runTest {
            every { getTrendingMoviesUseCase(1) } returns flowOf(Result.success(listOf(sampleMovie)))
            createViewModel(SectionType.TRENDING)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.screenState shouldBeInstanceOf SeeAllScreenState.Content::class
            state.movies.size shouldBeEqualTo 1
            state.movies.first().title shouldBeEqualTo "Inception"
        }

    @Test
    fun `GIVEN trending section WHEN LoadInitial error THEN state is Error`() =
        runTest {
            every { getTrendingMoviesUseCase(1) } returns
                flowOf(Result.failure(RuntimeException("Network error")))
            createViewModel(SectionType.TRENDING)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()

            viewModel.uiState.value.screenState shouldBeInstanceOf SeeAllScreenState.Error::class
        }

    @Test
    fun `GIVEN page flow throws cancellation WHEN LoadInitial THEN error and stale states are not created`() =
        runTest {
            every {
                getTrendingMoviesUseCase(1)
            } returns flow { throw CancellationException() }
            createViewModel(SectionType.TRENDING)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.screenState shouldBeInstanceOf SeeAllScreenState.Loading::class
            state.isShowingStaleData shouldBeEqualTo false
            state.movies shouldBeEqualTo emptyList()
        }

    @Test
    fun `GIVEN trending section WHEN LoadInitial empty THEN state is Empty`() =
        runTest {
            every { getTrendingMoviesUseCase(1) } returns flowOf(Result.success(emptyList()))
            createViewModel(SectionType.TRENDING)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()

            viewModel.uiState.value.screenState shouldBeInstanceOf SeeAllScreenState.Empty::class
        }

    @Test
    fun `GIVEN loaded page 1 WHEN LoadMore THEN appends movies and increments page`() =
        runTest {
            val secondMovie = sampleMovie.copy(id = 2, title = "Interstellar")
            every { getTrendingMoviesUseCase(1) } returns flowOf(Result.success(listOf(sampleMovie)))
            every { getTrendingMoviesUseCase(2) } returns flowOf(Result.success(listOf(secondMovie)))
            createViewModel(SectionType.TRENDING)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()

            viewModel.process(SeeAllMoviesIntent.LoadMore)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.movies.size shouldBeEqualTo 2
            state.movies[0].title shouldBeEqualTo "Inception"
            state.movies[1].title shouldBeEqualTo "Interstellar"
        }

    @Test
    fun `GIVEN page 2 repeats a movie WHEN LoadMore THEN movie IDs remain unique`() =
        runTest {
            val secondMovie = sampleMovie.copy(id = 2, title = "Interstellar")
            every { getTrendingMoviesUseCase(1) } returns flowOf(Result.success(listOf(sampleMovie)))
            every { getTrendingMoviesUseCase(2) } returns
                flowOf(Result.success(listOf(sampleMovie, secondMovie)))
            createViewModel(SectionType.TRENDING)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()
            viewModel.process(SeeAllMoviesIntent.LoadMore)
            advanceUntilIdle()

            viewModel.uiState.value.movies
                .map { movie -> movie.id } shouldBeEqualTo listOf(1, 2)
        }

    @Test
    fun `GIVEN loaded page 1 WHEN LoadMore returns empty THEN endReached is true`() =
        runTest {
            every { getTrendingMoviesUseCase(1) } returns flowOf(Result.success(listOf(sampleMovie)))
            every { getTrendingMoviesUseCase(2) } returns flowOf(Result.success(emptyList()))
            createViewModel(SectionType.TRENDING)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()

            viewModel.process(SeeAllMoviesIntent.LoadMore)
            advanceUntilIdle()

            viewModel.uiState.value.isEndReached shouldBeEqualTo true
        }

    @Test
    fun `GIVEN any section WHEN MovieClicked THEN emits NavigateToDetail effect`() =
        runTest {
            every { getTrendingMoviesUseCase(1) } returns flowOf(Result.success(listOf(sampleMovie)))
            createViewModel(SectionType.TRENDING)

            viewModel.effect.test {
                viewModel.process(SeeAllMoviesIntent.MovieClicked(42))

                val effect = awaitItem()
                effect shouldBeInstanceOf SeeAllMoviesEffect.NavigateToDetail::class
                (effect as SeeAllMoviesEffect.NavigateToDetail).movieId shouldBeEqualTo 42
            }
        }

    @Test
    fun `GIVEN error state WHEN Retry THEN reloads from page 1`() =
        runTest {
            every { getTrendingMoviesUseCase(1) } returns
                flowOf(Result.failure(RuntimeException("error")))
            createViewModel(SectionType.TRENDING)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()

            every { getTrendingMoviesUseCase.refresh(1) } returns
                flowOf(Result.success(listOf(sampleMovie)))

            viewModel.process(SeeAllMoviesIntent.Retry)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.screenState shouldBeInstanceOf SeeAllScreenState.Content::class
            state.movies.size shouldBeEqualTo 1
            verify(exactly = 1) { getTrendingMoviesUseCase.refresh(1) }
        }

    @Test
    fun `GIVEN popular section WHEN LoadInitial THEN calls GetPopularMoviesUseCase`() =
        runTest {
            every { getPopularMoviesUseCase(1) } returns flowOf(Result.success(listOf(sampleMovie)))
            createViewModel(SectionType.POPULAR)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()

            verify(exactly = 1) { getPopularMoviesUseCase(1) }
            verify(exactly = 0) { getTrendingMoviesUseCase(any()) }
        }

    @Test
    fun `GIVEN top rated section WHEN LoadInitial THEN calls GetTopRatedMoviesUseCase`() =
        runTest {
            every { getTopRatedMoviesUseCase(1) } returns flowOf(Result.success(listOf(sampleMovie)))
            createViewModel(SectionType.TOP_RATED)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()

            verify(exactly = 1) { getTopRatedMoviesUseCase(1) }
            verify(exactly = 0) { getTrendingMoviesUseCase(any()) }
        }

    @Test
    fun `GIVEN upcoming section WHEN LoadInitial THEN calls GetUpcomingMoviesUseCase`() =
        runTest {
            every { getUpcomingMoviesUseCase(1) } returns flowOf(Result.success(listOf(sampleMovie)))
            createViewModel(SectionType.UPCOMING)

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()

            verify(exactly = 1) { getUpcomingMoviesUseCase(1) }
            verify(exactly = 0) { getTrendingMoviesUseCase(any()) }
        }

    @Test
    fun `GIVEN page 2 is loading WHEN Retry completes THEN page 2 cannot overwrite retried content`() =
        runTest {
            val page2Results = MutableSharedFlow<Result<List<Movie>>>(extraBufferCapacity = 1)
            val retriedMovie = sampleMovie.copy(id = 2, title = "Interstellar")
            every { getTrendingMoviesUseCase(1) } returns
                flowOf(Result.success(listOf(sampleMovie)))
            every { getTrendingMoviesUseCase.refresh(1) } returns
                flowOf(Result.success(listOf(retriedMovie)))
            every { getTrendingMoviesUseCase(2) } returns page2Results
            createViewModel()

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()
            viewModel.process(SeeAllMoviesIntent.LoadMore)
            runCurrent()
            viewModel.process(SeeAllMoviesIntent.Retry)
            advanceUntilIdle()
            page2Results.emit(Result.success(listOf(sampleMovie.copy(id = 3, title = "Old page"))))
            advanceUntilIdle()

            viewModel.uiState.value.movies
                .map { movie -> movie.title } shouldBeEqualTo
                listOf("Interstellar")
        }

    @Test
    fun `GIVEN stale page 2 data WHEN stale signal follows THEN pagination remains available`() =
        runTest {
            val secondMovie = sampleMovie.copy(id = 2, title = "Interstellar")
            every { getTrendingMoviesUseCase(1) } returns flowOf(Result.success(listOf(sampleMovie)))
            every { getTrendingMoviesUseCase(2) } returns
                flow {
                    emit(Result.success(listOf(secondMovie)))
                    emit(Result.failure(StaleDataException()))
                }
            createViewModel()

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()
            viewModel.process(SeeAllMoviesIntent.LoadMore)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.movies.map { movie -> movie.title } shouldBeEqualTo
                listOf("Inception", "Interstellar")
            state.isShowingStaleData shouldBeEqualTo true
            state.isEndReached shouldBeEqualTo false
            state.isLoadingMore shouldBeEqualTo false
        }

    @Test
    fun `GIVEN stale page 1 WHEN page 2 is fresh THEN stale banner remains visible`() =
        runTest {
            val secondMovie = sampleMovie.copy(id = 2, title = "Interstellar")
            every { getTrendingMoviesUseCase(1) } returns
                flow {
                    emit(Result.success(listOf(sampleMovie)))
                    emit(Result.failure(StaleDataException()))
                }
            every { getTrendingMoviesUseCase(2) } returns
                flowOf(Result.success(listOf(secondMovie)))
            createViewModel()

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()
            viewModel.process(SeeAllMoviesIntent.LoadMore)
            advanceUntilIdle()

            viewModel.uiState.value.isShowingStaleData shouldBeEqualTo true
        }

    @Test
    fun `GIVEN stale content WHEN Retry returns fresh data THEN stale banner is cleared`() =
        runTest {
            every { getTrendingMoviesUseCase(1) } returns
                flow {
                    emit(Result.success(listOf(sampleMovie)))
                    emit(Result.failure(StaleDataException()))
                }
            every { getTrendingMoviesUseCase.refresh(1) } returns
                flowOf(Result.success(listOf(sampleMovie)))
            createViewModel()

            viewModel.process(SeeAllMoviesIntent.LoadInitial)
            advanceUntilIdle()
            viewModel.uiState.value.isShowingStaleData shouldBeEqualTo true

            viewModel.process(SeeAllMoviesIntent.Retry)
            advanceUntilIdle()

            viewModel.uiState.value.isShowingStaleData shouldBeEqualTo false
        }

    @Test
    fun `GIVEN no active collector WHEN navigation effect is emitted THEN it is not replayed`() =
        runTest {
            createViewModel()
            viewModel.process(SeeAllMoviesIntent.MovieClicked(42))
            advanceUntilIdle()

            viewModel.effect.test {
                expectNoEvents()
            }
        }

    @Test
    fun `GIVEN active collector WHEN navigation effect is emitted THEN it is delivered`() =
        runTest {
            createViewModel()

            viewModel.effect.test {
                viewModel.process(SeeAllMoviesIntent.MovieClicked(42))
                runCurrent()

                awaitItem() shouldBeEqualTo SeeAllMoviesEffect.NavigateToDetail(42)
            }
        }
}
