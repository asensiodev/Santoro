package com.asensiodev.santoro.core.database.data.repository

import app.cash.turbine.test
import com.asensiodev.core.domain.Movie
import com.asensiodev.core.domain.Result
import com.asensiodev.santoro.core.database.data.MockUtils
import com.asensiodev.santoro.core.database.data.dao.MovieDao
import com.asensiodev.santoro.core.database.data.mapper.toDomain
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomDatabaseRepositoryTest {
    private val movieDao: MovieDao = mockk()

    private lateinit var repository: RoomDatabaseRepository

    @BeforeEach
    fun setUp() {
        repository = RoomDatabaseRepository(movieDao)
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
                awaitItem() shouldBeEqualTo Result.Loading
                awaitItem() shouldBeEqualTo Result.Success(movieEntities.map { it.toDomain() })
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN no watched movies WHEN getWatchedMovies THEN returns empty list`() =
        runTest {
            every { movieDao.getWatchedMovies() } returns flowOf(emptyList())
            repository.getWatchedMovies().test {
                awaitItem() shouldBeEqualTo Result.Loading
                awaitItem() shouldBeEqualTo Result.Success(emptyList())
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
                awaitItem() shouldBeEqualTo Result.Loading
                awaitItem() shouldBeEqualTo Result.Success(watchlistEntities.map { it.toDomain() })
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN no watchlist movies WHEN getWatchlistMovies THEN returns empty list`() =
        runTest {
            every { movieDao.getWatchlistMovies() } returns flowOf(emptyList())
            repository.getWatchlistMovies().test {
                awaitItem() shouldBeEqualTo Result.Loading
                awaitItem() shouldBeEqualTo Result.Success(emptyList())
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN existing movieId WHEN getMovieById THEN returns movie`() =
        runTest {
            val entity = MockUtils.createTestMovieEntity(id = 200, title = "Movie 200", isWatched = true)
            coEvery { movieDao.getMovieById(200) } returns entity
            val result = repository.getMovieById(200)
            result shouldBeEqualTo Result.Success(entity.toDomain())
        }

    @Test
    fun `GIVEN non existing movieId WHEN getMovieById THEN returns null`() =
        runTest {
            coEvery { movieDao.getMovieById(999) } returns null
            val result = repository.getMovieById(999)
            result shouldBeEqualTo Result.Success(null)
        }

    @Test
    fun `GIVEN dao throws exception WHEN getMovieById THEN returns error`() =
        runTest {
            coEvery { movieDao.getMovieById(300) } throws RuntimeException("Error")
            val result = repository.getMovieById(300)
            val error = result as? Result.Error
            error.shouldNotBeNull()
            error.exception.shouldBeInstanceOf<RuntimeException>()
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
                awaitItem() shouldBeEqualTo Result.Loading
                awaitItem() shouldBeEqualTo Result.Success(movieEntities.map { it.toDomain() })
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
                awaitItem() shouldBeEqualTo Result.Loading
                awaitItem() shouldBeEqualTo Result.Success(watchlistEntities.map { it.toDomain() })
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN no results WHEN searchWatchlistMoviesByTitle THEN returns empty list`() =
        runTest {
            val query = "NoMatch"
            every { movieDao.searchWatchlistMoviesByTitle(query) } returns flowOf(emptyList())
            repository.searchWatchlistMoviesByTitle(query).test {
                awaitItem() shouldBeEqualTo Result.Loading
                awaitItem() shouldBeEqualTo Result.Success(emptyList())
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN a movie to update WHEN updateMovieState THEN returns true`() =
        runTest {
            coEvery { movieDao.insertOrUpdateMovie(any()) } just runs
            val domainMovie =
                Movie(
                    id = 400,
                    title = "UpdateMe",
                    overview = "Updated",
                    posterPath = null,
                    releaseDate = null,
                    popularity = 8.9,
                    voteAverage = 10.11,
                    voteCount = 2964,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = false,
                    isInWatchlist = false,
                )
            val result = repository.updateMovieState(domainMovie)
            result shouldBeEqualTo Result.Success(true)
        }

    @Test
    fun `GIVEN update movie throws exception WHEN updateMovieState THEN returns error`() =
        runTest {
            coEvery { movieDao.insertOrUpdateMovie(any()) } throws RuntimeException("DB error")
            val domainMovie =
                Movie(
                    id = 401,
                    title = "UpdateMe",
                    overview = "Updated",
                    posterPath = null,
                    releaseDate = null,
                    popularity = 8.9,
                    voteAverage = 10.11,
                    voteCount = 2964,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = false,
                    isInWatchlist = false,
                )
            val result = repository.updateMovieState(domainMovie)
            val error = result as? Result.Error
            error.shouldNotBeNull()
            error.exception.shouldBeInstanceOf<RuntimeException>()
        }
}
