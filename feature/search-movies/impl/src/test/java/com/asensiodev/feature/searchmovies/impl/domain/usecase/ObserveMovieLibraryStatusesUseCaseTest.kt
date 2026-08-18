package com.asensiodev.feature.searchmovies.impl.domain.usecase

import app.cash.turbine.test
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.feature.searchmovies.impl.domain.model.MovieLibraryStatus
import com.asensiodev.feature.searchmovies.impl.domain.repository.MovieLibraryStatusRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ObserveMovieLibraryStatusesUseCaseTest {
    private val repository: MovieLibraryStatusRepository = mockk()

    @Test
    fun `GIVEN empty sets WHEN observing THEN emits empty map`() =
        runTest {
            every { repository.observeWatchedMovieIds() } returns flowOf(Result.success(emptySet()))
            every { repository.observeWatchlistMovieIds() } returns flowOf(Result.success(emptySet()))

            createUseCase()().test {
                awaitItem() shouldBeEqualTo Result.success(emptyMap())
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN watched and watchlist IDs WHEN observing THEN emits their statuses`() =
        runTest {
            every { repository.observeWatchedMovieIds() } returns flowOf(Result.success(setOf(1)))
            every { repository.observeWatchlistMovieIds() } returns flowOf(Result.success(setOf(2)))

            createUseCase()().test {
                awaitItem() shouldBeEqualTo
                    Result.success(
                        mapOf(
                            1 to MovieLibraryStatus.Watched,
                            2 to MovieLibraryStatus.Watchlist,
                        ),
                    )
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN dual membership WHEN observing THEN watched wins`() =
        runTest {
            every { repository.observeWatchedMovieIds() } returns flowOf(Result.success(setOf(1)))
            every { repository.observeWatchlistMovieIds() } returns flowOf(Result.success(setOf(1)))

            createUseCase()().test {
                awaitItem() shouldBeEqualTo Result.success(mapOf(1 to MovieLibraryStatus.Watched))
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN changed source emissions WHEN observing THEN emits updated maps`() =
        runTest {
            val watched = MutableSharedFlow<Result<Set<Int>>>(replay = 1)
            val watchlist = MutableSharedFlow<Result<Set<Int>>>(replay = 1)
            every { repository.observeWatchedMovieIds() } returns watched
            every { repository.observeWatchlistMovieIds() } returns watchlist

            createUseCase()().test {
                watched.emit(Result.success(setOf(1)))
                watchlist.emit(Result.success(setOf(2)))
                awaitItem() shouldBeEqualTo
                    Result.success(
                        mapOf(
                            1 to MovieLibraryStatus.Watched,
                            2 to MovieLibraryStatus.Watchlist,
                        ),
                    )
                watched.emit(Result.success(emptySet()))
                awaitItem() shouldBeEqualTo Result.success(mapOf(2 to MovieLibraryStatus.Watchlist))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN source failure and recovery WHEN observing THEN emits failure then recovered map`() =
        runTest {
            val exception = IllegalStateException()
            val watched = MutableSharedFlow<Result<Set<Int>>>(replay = 1)
            val watchlist = MutableSharedFlow<Result<Set<Int>>>(replay = 1)
            every { repository.observeWatchedMovieIds() } returns watched
            every { repository.observeWatchlistMovieIds() } returns watchlist

            createUseCase()().test {
                watched.emit(Result.success(setOf(1)))
                watchlist.emit(Result.failure(exception))
                awaitItem() shouldBeEqualTo Result.failure(exception)
                watchlist.emit(Result.success(setOf(2)))
                awaitItem() shouldBeEqualTo
                    Result.success(
                        mapOf(
                            1 to MovieLibraryStatus.Watched,
                            2 to MovieLibraryStatus.Watchlist,
                        ),
                    )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN watched failure WHEN observing THEN emits failure`() =
        runTest {
            val exception = IllegalStateException()
            every { repository.observeWatchedMovieIds() } returns flowOf(Result.failure(exception))
            every { repository.observeWatchlistMovieIds() } returns flowOf(Result.success(emptySet()))

            createUseCase()().test {
                awaitItem() shouldBeEqualTo Result.failure(exception)
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN wrapped cancellation WHEN observing THEN cancellation propagates`() =
        runTest {
            every { repository.observeWatchedMovieIds() } returns
                flowOf(Result.failure(CancellationException()))
            every { repository.observeWatchlistMovieIds() } returns flowOf(Result.success(emptySet()))

            createUseCase()().test {
                awaitError().shouldBeInstanceOf<CancellationException>()
            }
        }

    @Test
    fun `GIVEN cancellation in second result WHEN first result fails THEN cancellation propagates`() =
        runTest {
            every { repository.observeWatchedMovieIds() } returns
                flowOf(Result.failure(IllegalStateException()))
            every { repository.observeWatchlistMovieIds() } returns
                flowOf(Result.failure(CancellationException()))

            createUseCase()().test {
                awaitError().shouldBeInstanceOf<CancellationException>()
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.createUseCase() =
        ObserveMovieLibraryStatusesUseCase(
            repository,
            TestDispatcherProvider(
                io = UnconfinedTestDispatcher(testScheduler),
                default = UnconfinedTestDispatcher(testScheduler),
                main = UnconfinedTestDispatcher(testScheduler),
            ),
        )
}
