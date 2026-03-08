package com.asensiodev.feature.watchedmovies.impl.domain.usecase

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset

class GetWatchedStatsUseCaseTest {
    private val repository: DatabaseRepository = mockk()
    private val dispatchers = TestDispatcherProvider()

    private lateinit var useCase: GetWatchedStatsUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetWatchedStatsUseCase(repository, dispatchers)
    }

    @Test
    fun `GIVEN empty list WHEN invoke THEN returns zero stats`() =
        runTest {
            // GIVEN
            every { repository.getWatchedMovies() } returns flowOf(Result.success(emptyList()))

            // WHEN
            val result = useCase().toList().first()

            // THEN
            result.totalWatched shouldBeEqualTo 0
            result.totalRuntimeHours shouldBeEqualTo 0
            result.favouriteGenre.shouldBeNull()
            result.longestStreakWeeks shouldBeEqualTo 0
        }

    @Test
    fun `GIVEN movies with runtimes WHEN invoke THEN totalRuntimeHours is sum divided by 60`() =
        runTest {
            // GIVEN
            val movies =
                listOf(
                    buildMovie(id = 1, runtime = 120),
                    buildMovie(id = 2, runtime = 90),
                )
            every { repository.getWatchedMovies() } returns flowOf(Result.success(movies))

            // WHEN
            val result = useCase().toList().first()

            // THEN
            result.totalRuntimeHours shouldBeEqualTo 3
        }

    @Test
    fun `GIVEN movies with null runtimes WHEN invoke THEN totalRuntimeHours is 0`() =
        runTest {
            // GIVEN
            val movies =
                listOf(
                    buildMovie(id = 1, runtime = null),
                    buildMovie(id = 2, runtime = null),
                )
            every { repository.getWatchedMovies() } returns flowOf(Result.success(movies))

            // WHEN
            val result = useCase().toList().first()

            // THEN
            result.totalRuntimeHours shouldBeEqualTo 0
        }

    @Test
    fun `GIVEN movies with genres WHEN invoke THEN favouriteGenre is most frequent`() =
        runTest {
            // GIVEN
            val action = Genre(id = 1, name = "Action")
            val drama = Genre(id = 2, name = "Drama")
            val movies =
                listOf(
                    buildMovie(id = 1, genres = listOf(action, drama)),
                    buildMovie(id = 2, genres = listOf(action)),
                    buildMovie(id = 3, genres = listOf(drama)),
                )
            every { repository.getWatchedMovies() } returns flowOf(Result.success(movies))

            // WHEN
            val result = useCase().toList().first()

            // THEN
            result.favouriteGenre shouldBeEqualTo "Action"
        }

    @Test
    fun `GIVEN movies with no genres WHEN invoke THEN favouriteGenre is null`() =
        runTest {
            // GIVEN
            val movies =
                listOf(
                    buildMovie(id = 1, genres = emptyList()),
                    buildMovie(id = 2, genres = emptyList()),
                )
            every { repository.getWatchedMovies() } returns flowOf(Result.success(movies))

            // WHEN
            val result = useCase().toList().first()

            // THEN
            result.favouriteGenre.shouldBeNull()
        }

    @Test
    fun `GIVEN movies watched on consecutive weeks WHEN invoke THEN longestStreakWeeks is correct`() =
        runTest {
            val week1 = localDateToMillis(LocalDate.of(2024, 1, 1))
            val week2 = localDateToMillis(LocalDate.of(2024, 1, 8))
            val week3 = localDateToMillis(LocalDate.of(2024, 1, 15))
            val movies =
                listOf(
                    buildMovie(id = 1, watchedAt = week1),
                    buildMovie(id = 2, watchedAt = week2),
                    buildMovie(id = 3, watchedAt = week3),
                )
            every { repository.getWatchedMovies() } returns flowOf(Result.success(movies))

            val result = useCase().toList().first()

            result.longestStreakWeeks shouldBeEqualTo 3
        }

    @Test
    fun `GIVEN movies watched in a single week WHEN invoke THEN longestStreakWeeks is 0`() =
        runTest {
            val sameWeekDay1 = localDateToMillis(LocalDate.of(2024, 1, 1))
            val sameWeekDay2 = localDateToMillis(LocalDate.of(2024, 1, 3))
            val movies =
                listOf(
                    buildMovie(id = 1, watchedAt = sameWeekDay1),
                    buildMovie(id = 2, watchedAt = sameWeekDay2),
                )
            every { repository.getWatchedMovies() } returns flowOf(Result.success(movies))

            val result = useCase().toList().first()

            result.longestStreakWeeks shouldBeEqualTo 0
        }

    @Test
    fun `GIVEN movies watched on non-consecutive weeks WHEN invoke THEN streak resets`() =
        runTest {
            // GIVEN — week 1, gap, then weeks 3 and 4 consecutive
            val week1 = localDateToMillis(LocalDate.of(2024, 1, 1))
            val week3 = localDateToMillis(LocalDate.of(2024, 1, 15))
            val week4 = localDateToMillis(LocalDate.of(2024, 1, 22))
            val movies =
                listOf(
                    buildMovie(id = 1, watchedAt = week1),
                    buildMovie(id = 2, watchedAt = week3),
                    buildMovie(id = 3, watchedAt = week4),
                )
            every { repository.getWatchedMovies() } returns flowOf(Result.success(movies))

            // WHEN
            val result = useCase().toList().first()

            // THEN
            result.longestStreakWeeks shouldBeEqualTo 2
        }

    @Test
    fun `GIVEN repository error WHEN invoke THEN returns zero stats`() =
        runTest {
            // GIVEN
            every { repository.getWatchedMovies() } returns
                flowOf(
                    Result.failure(RuntimeException("DB error")),
                )

            // WHEN
            val result = useCase().toList().first()

            // THEN
            result.totalWatched shouldBeEqualTo 0
            result.totalRuntimeHours shouldBeEqualTo 0
            result.favouriteGenre.shouldBeNull()
            result.longestStreakWeeks shouldBeEqualTo 0
        }

    private fun buildMovie(
        id: Int,
        runtime: Int? = null,
        genres: List<Genre> = emptyList(),
        watchedAt: Long? = null,
    ): Movie =
        Movie(
            id = id,
            title = "Movie $id",
            overview = "",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            popularity = 0.0,
            voteAverage = 0.0,
            voteCount = 0,
            genres = genres,
            productionCountries = emptyList(),
            runtime = runtime,
            isWatched = true,
            isInWatchlist = false,
            watchedAt = watchedAt,
        )

    private fun localDateToMillis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}
