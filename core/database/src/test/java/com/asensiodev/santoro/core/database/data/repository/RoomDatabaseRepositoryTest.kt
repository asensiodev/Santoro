package com.asensiodev.santoro.core.database.data.repository

import app.cash.turbine.test
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.santoro.core.database.data.MockUtils
import com.asensiodev.santoro.core.database.data.SantoroRoomDatabase
import com.asensiodev.santoro.core.database.data.dao.MovieDao
import com.asensiodev.santoro.core.database.data.mapper.toDomain
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomDatabaseRepositoryTest {
    private val database: SantoroRoomDatabase = mockk()
    private val movieDao: MovieDao = mockk()

    private lateinit var repository: RoomDatabaseRepository

    @BeforeEach
    fun setUp() {
        every { database.movieDao() } returns movieDao
        repository = RoomDatabaseRepository(database)
    }

    @Test
    fun `GIVEN watched movies WHEN getWatchedMovies THEN returns watched movies`() =
        runTest {
            val movieEntities =
                listOf(
                    MockUtils.createTestMovieEntity(id = 1, title = "Watched 1", isWatched = true),
                    MockUtils.createTestMovieEntity(id = 2, title = "Watched 2", isWatched = true),
                )
            every { movieDao.getWatchedMovies() } returns flowOf(movieEntities)
            repository.getWatchedMovies().test {
                awaitItem() shouldBeEqualTo Result.success(movieEntities.map { it.toDomain() })
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN no watched movies WHEN getWatchedMovies THEN returns empty list`() =
        runTest {
            every { movieDao.getWatchedMovies() } returns flowOf(emptyList())
            repository.getWatchedMovies().test {
                awaitItem() shouldBeEqualTo Result.success(emptyList<Movie>())
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN watchlist movies WHEN getWatchlistMovies THEN returns watchlist movies`() =
        runTest {
            val watchlistEntities =
                listOf(
                    MockUtils.createTestMovieEntity(id = 100, title = "Watchlist 1", isInWatchlist = true),
                    MockUtils.createTestMovieEntity(id = 101, title = "Watchlist 2", isInWatchlist = true),
                )
            every { movieDao.getWatchlistMovies() } returns flowOf(watchlistEntities)
            repository.getWatchlistMovies().test {
                awaitItem() shouldBeEqualTo Result.success(watchlistEntities.map { it.toDomain() })
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN no watchlist movies WHEN getWatchlistMovies THEN returns empty list`() =
        runTest {
            every { movieDao.getWatchlistMovies() } returns flowOf(emptyList())
            repository.getWatchlistMovies().test {
                awaitItem() shouldBeEqualTo Result.success(emptyList<Movie>())
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN watchlist flow is cancelled WHEN collected THEN cancellation propagates`() =
        runTest {
            every { movieDao.getWatchlistMovies() } returns flow { throw CancellationException() }

            repository.getWatchlistMovies().test {
                awaitError().shouldBeInstanceOf<CancellationException>()
            }
        }

    @Test
    fun `GIVEN existing movieId WHEN getMovieById THEN returns expected movie`() =
        runTest {
            val entity = MockUtils.createTestMovieEntity(id = 200, title = "Movie 200", isWatched = true)
            coEvery { movieDao.getMovieById(200) } returns entity
            val result = repository.getMovieById(200)
            result shouldBeEqualTo Result.success(entity.toDomain())
        }

    @Test
    fun `GIVEN non existing movieId WHEN getMovieById THEN returns null`() =
        runTest {
            coEvery { movieDao.getMovieById(999) } returns null
            val result = repository.getMovieById(999)
            result shouldBeEqualTo Result.success(null)
        }

    @Test
    fun `GIVEN dao throws exception WHEN getMovieById THEN returns error`() =
        runTest {
            coEvery { movieDao.getMovieById(300) } throws RuntimeException("Error")
            val result = repository.getMovieById(300)
            result.isFailure shouldBeEqualTo true
            result.exceptionOrNull().shouldBeInstanceOf<RuntimeException>()
        }

    @Test
    fun `GIVEN dao cancels WHEN getMovieById THEN cancellation propagates`() =
        runTest {
            coEvery { movieDao.getMovieById(300) } throws CancellationException()

            val exception =
                try {
                    repository.getMovieById(300)
                    null
                } catch (exception: CancellationException) {
                    exception
                }

            exception.shouldBeInstanceOf<CancellationException>()
        }

    @Test
    fun `GIVEN a query WHEN searchWatchedMoviesByTitle THEN returns matching movies`() =
        runTest {
            val query = "Inception"
            val movieEntities =
                listOf(
                    MockUtils.createTestMovieEntity(id = 10, title = "Inception Part I", isWatched = true),
                    MockUtils.createTestMovieEntity(id = 11, title = "Inception Part II", isWatched = true),
                )
            every { movieDao.searchWatchedMoviesByTitle(query) } returns flowOf(movieEntities)
            repository.searchWatchedMoviesByTitle(query).test {
                awaitItem() shouldBeEqualTo Result.success(movieEntities.map { it.toDomain() })
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN a query WHEN searchWatchlistMoviesByTitle THEN returns matching movies`() =
        runTest {
            val query = "Batman"
            val watchlistEntities =
                listOf(
                    MockUtils.createTestMovieEntity(id = 301, title = "Batman Begins", isInWatchlist = true),
                    MockUtils.createTestMovieEntity(id = 302, title = "Batman: The Dark Knight", isInWatchlist = true),
                )
            every { movieDao.searchWatchlistMoviesByTitle(query) } returns flowOf(watchlistEntities)
            repository.searchWatchlistMoviesByTitle(query).test {
                awaitItem() shouldBeEqualTo Result.success(watchlistEntities.map { it.toDomain() })
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN no results WHEN searchWatchlistMoviesByTitle THEN returns empty list`() =
        runTest {
            val query = "NoMatch"
            every { movieDao.searchWatchlistMoviesByTitle(query) } returns flowOf(emptyList())
            repository.searchWatchlistMoviesByTitle(query).test {
                awaitItem() shouldBeEqualTo Result.success(emptyList<Movie>())
                awaitComplete()
            }
        }
}
