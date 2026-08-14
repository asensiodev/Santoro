package com.asensiodev.santoro.core.database.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.santoro.core.database.MockUtils
import com.asensiodev.santoro.core.database.data.SantoroRoomDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieDaoTest {
    private lateinit var database: SantoroRoomDatabase
    private lateinit var movieDao: MovieDao

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    SantoroRoomDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        movieDao = database.movieDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertOrUpdateMovie_thenGetMovieById() {
        runBlocking {
            val movie =
                MockUtils.createTestMovieEntity(
                    id = 1,
                    title = "Matrix",
                    isInWatchlist = true,
                    isWatched = false,
                    genres = """["Action","Sci-Fi"]""",
                    productionCountries = """["USA","Australia"]""",
                )
            movieDao.insertOrUpdateMovie(movie)
            val retrievedMovie = movieDao.getMovieById(1)
            retrievedMovie.shouldNotBeNull()
            retrievedMovie.title shouldBeEqualTo "Matrix"
            retrievedMovie.isInWatchlist.shouldBeTrue()
            retrievedMovie.genres shouldBeEqualTo """["Action","Sci-Fi"]"""
            retrievedMovie.productionCountries shouldBeEqualTo """["USA","Australia"]"""
        }
    }

    @Test
    fun getWatchlistMovies_returnsOnlyMoviesInWatchlist() {
        runBlocking {
            val movies =
                listOf(
                    MockUtils.createTestMovieEntity(
                        id = 1,
                        title = "Film 1",
                        isInWatchlist = true,
                        isWatched = false,
                        genres = """["Action"]""",
                        productionCountries = """["USA"]""",
                    ),
                    MockUtils.createTestMovieEntity(
                        id = 2,
                        title = "Film 2",
                        isInWatchlist = false,
                        isWatched = false,
                        genres = """["Drama"]""",
                        productionCountries = """["France"]""",
                    ),
                    MockUtils.createTestMovieEntity(
                        id = 3,
                        title = "Film 3",
                        isInWatchlist = true,
                        isWatched = true,
                        genres = """["Comedy"]""",
                        productionCountries = """["Italy"]""",
                    ),
                )
            movies.forEach { movieDao.insertOrUpdateMovie(it) }
            val watchlistMovies = movieDao.getWatchlistMovies().first()
            watchlistMovies.size shouldBeEqualTo 2
            val titles = watchlistMovies.map { it.title }
            titles shouldContain "Film 1"
            titles shouldContain "Film 3"
        }
    }

    @Test
    fun getWatchedMovies_returnsOnlyWatched() {
        runBlocking {
            val movie1 =
                MockUtils.createTestMovieEntity(
                    id = 101,
                    title = "Watched 1",
                    isWatched = true,
                )
            val movie2 =
                MockUtils.createTestMovieEntity(
                    id = 102,
                    title = "Unwatched",
                    isWatched = false,
                )
            val movie3 =
                MockUtils.createTestMovieEntity(
                    id = 103,
                    title = "Watched 2",
                    isWatched = true,
                )
            movieDao.insertOrUpdateMovie(movie1)
            movieDao.insertOrUpdateMovie(movie2)
            movieDao.insertOrUpdateMovie(movie3)
            val watchedMovies = movieDao.getWatchedMovies().first()
            watchedMovies.size shouldBeEqualTo 2
            val titles = watchedMovies.map { it.title }
            titles shouldContain "Watched 1"
            titles shouldContain "Watched 2"
        }
    }

    @Test
    fun getMoviesForSync_returnsMoviesWithLocalChangesEvenWhenUnmarked() {
        runBlocking {
            val activeMovie =
                MockUtils.createTestMovieEntity(
                    id = 111,
                    title = "Active",
                    isWatched = true,
                    updatedAt = 1000L,
                )
            val unmarkedMovie =
                MockUtils.createTestMovieEntity(
                    id = 112,
                    title = "Unmarked",
                    isWatched = false,
                    isInWatchlist = false,
                    updatedAt = 2000L,
                )
            val untouchedMovie =
                MockUtils.createTestMovieEntity(
                    id = 113,
                    title = "Untouched",
                    isWatched = false,
                    isInWatchlist = false,
                    updatedAt = 0L,
                )
            movieDao.insertOrUpdateMovie(activeMovie)
            movieDao.insertOrUpdateMovie(unmarkedMovie)
            movieDao.insertOrUpdateMovie(untouchedMovie)

            val movies = movieDao.getMoviesForSync()

            movies.size shouldBeEqualTo 2
            movies.map { it.id } shouldContain 111
            movies.map { it.id } shouldContain 112
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun searchWatchedMoviesByTitle_returnsFilteredResults() {
        runBlocking {
            val movies =
                listOf(
                    MockUtils.createTestMovieEntity(
                        id = 201,
                        title = "Matrix Reloaded",
                        isWatched = true,
                        genres = """["Action","Sci-Fi"]""",
                        productionCountries = """["USA"]""",
                    ),
                    MockUtils.createTestMovieEntity(
                        id = 202,
                        title = "Matrix Revolutions",
                        isWatched = true,
                        genres = """["Action","Sci-Fi"]""",
                        productionCountries = """["Australia"]""",
                    ),
                    MockUtils.createTestMovieEntity(
                        id = 203,
                        title = "Avatar",
                        isWatched = true,
                        genres = """["Fantasy","Sci-Fi"]""",
                        productionCountries = """["USA"]""",
                    ),
                    MockUtils.createTestMovieEntity(
                        id = 204,
                        title = "Titanic",
                        isWatched = false,
                        genres = """["Drama"]""",
                        productionCountries = """["USA","UK"]""",
                    ),
                )
            movies.forEach { movieDao.insertOrUpdateMovie(it) }
            val foundMovies = movieDao.searchWatchedMoviesByTitle("matrix").first()
            foundMovies.size shouldBeEqualTo 2
            foundMovies.any { it.title == "Matrix Reloaded" }.shouldBeTrue()
            foundMovies.any { it.title == "Matrix Revolutions" }.shouldBeTrue()
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun updateMovie_updatesFieldsCorrectly() {
        runBlocking {
            val movie =
                MockUtils.createTestMovieEntity(
                    id = 301,
                    title = "Old Title",
                    isWatched = false,
                    genres = """["Action"]""",
                    productionCountries = """["USA"]""",
                )
            movieDao.insertOrUpdateMovie(movie)
            val updatedMovie =
                movie.copy(
                    title = "New Title",
                    isWatched = true,
                    genres = """["Action","Thriller"]""",
                    productionCountries = """["USA","Canada"]""",
                )
            movieDao.updateMovie(updatedMovie)
            val fromDb = movieDao.getMovieById(301)
            fromDb.shouldNotBeNull()
            fromDb.title shouldBeEqualTo "New Title"
            fromDb.isWatched.shouldBeTrue()
            fromDb.genres shouldBeEqualTo """["Action","Thriller"]"""
            fromDb.productionCountries shouldBeEqualTo """["USA","Canada"]"""
        }
    }

    @Test
    fun givenMixedMovieStates_whenWatchlistSearched_thenMatchingWatchlistTitlesReturned() {
        runBlocking {
            val movies =
                listOf(
                    MockUtils.createTestMovieEntity(401, "The Matrix", isInWatchlist = true),
                    MockUtils.createTestMovieEntity(402, "Matrix Reloaded", isInWatchlist = true),
                    MockUtils.createTestMovieEntity(
                        403,
                        "Matrix Revolutions",
                        isInWatchlist = false,
                    ),
                    MockUtils.createTestMovieEntity(404, "Arrival", isInWatchlist = true),
                )
            movies.forEach { movie -> movieDao.insertOrUpdateMovie(movie) }

            val result = movieDao.searchWatchlistMoviesByTitle("mAtRiX").first()

            result.map { movie -> movie.id }.sorted() shouldBeEqualTo listOf(401, 402)
        }
    }

    @Test
    fun givenWatchlistMovie_whenRemoved_thenTimestampChangesAndUnrelatedFieldsRemain() {
        runBlocking {
            val movie =
                MockUtils.createTestMovieEntity(
                    id = 501,
                    title = "Removal",
                    isWatched = true,
                    isInWatchlist = true,
                    overview = "Preserved overview",
                    posterPath = "/preserved.jpg",
                    tagline = "Preserved tagline",
                    runtime = 123,
                    watchedAt = 700L,
                    updatedAt = 600L,
                )
            movieDao.insertOrUpdateMovie(movie)

            movieDao.removeFromWatchlist(movie.id, 800L)

            movieDao.getMovieById(movie.id) shouldBeEqualTo
                movie.copy(isInWatchlist = false, updatedAt = 800L)
            movieDao.getMoviesForSync().map { synced -> synced.id } shouldContain movie.id
        }
    }

    @Test
    fun givenRemoteMovie_whenSyncUpserted_thenFieldsAndSqlDefaultsStored() {
        runBlocking {
            movieDao.upsertMovieFromSync(
                movieId = 601,
                title = "Synced",
                posterPath = "/synced.jpg",
                genres = "[\"Drama\"]",
                runtime = 140,
                isWatched = true,
                isInWatchlist = false,
                watchedAt = 900L,
                updatedAt = 1_000L,
            )

            movieDao.getMovieById(601) shouldBeEqualTo
                com.asensiodev.santoro.core.database.data.model.MovieEntity(
                    id = 601,
                    title = "Synced",
                    overview = "",
                    posterPath = "/synced.jpg",
                    releaseDate = null,
                    popularity = 0.0,
                    voteAverage = 0.0,
                    voteCount = 0,
                    genres = "[\"Drama\"]",
                    productionCountries = "",
                    tagline = null,
                    runtime = 140,
                    isWatched = true,
                    isInWatchlist = false,
                    watchedAt = 900L,
                    updatedAt = 1_000L,
                )
        }
    }

    @Test
    fun givenRichMovie_whenSyncStateUpdated_thenContentFieldsRemainUnchanged() {
        runBlocking {
            val movie =
                MockUtils.createTestMovieEntity(
                    id = 701,
                    title = "State update",
                    overview = "Rich overview",
                    posterPath = "/rich.jpg",
                    releaseDate = "2025-01-01",
                    tagline = "Rich tagline",
                    runtime = 110,
                    updatedAt = 100L,
                )
            movieDao.insertOrUpdateMovie(movie)

            movieDao.updateMovieSyncState(
                movieId = movie.id,
                isWatched = true,
                isInWatchlist = true,
                watchedAt = 200L,
                updatedAt = 300L,
            )

            movieDao.getMovieById(movie.id) shouldBeEqualTo
                movie.copy(
                    isWatched = true,
                    isInWatchlist = true,
                    watchedAt = 200L,
                    updatedAt = 300L,
                )
        }
    }

    @Test
    fun givenWatchedMovies_whenObserved_thenNewestWatchedTimestampIsFirst() {
        runBlocking {
            val movies =
                listOf(
                    MockUtils.createTestMovieEntity(801, isWatched = true, watchedAt = 100L),
                    MockUtils.createTestMovieEntity(802, isWatched = true, watchedAt = 300L),
                    MockUtils.createTestMovieEntity(803, isWatched = true, watchedAt = 200L),
                    MockUtils.createTestMovieEntity(804, isWatched = false, watchedAt = 400L),
                )
            movies.forEach { movie -> movieDao.insertOrUpdateMovie(movie) }

            val result = movieDao.getWatchedMovies().first()

            result.map { movie -> movie.id } shouldBeEqualTo listOf(802, 803, 801)
        }
    }

    @Test
    fun givenStoredMovies_whenUserDataCleared_thenEveryMovieIsRemoved() {
        runBlocking {
            movieDao.insertOrUpdateMovie(MockUtils.createTestMovieEntity(901, isWatched = true))
            movieDao.insertOrUpdateMovie(MockUtils.createTestMovieEntity(902, isInWatchlist = true))
            movieDao.insertOrUpdateMovie(MockUtils.createTestMovieEntity(903))

            movieDao.clearAllUserData()

            movieDao.getWatchedMovies().first().isEmpty() shouldBeEqualTo true
            movieDao.getWatchlistMovies().first().isEmpty() shouldBeEqualTo true
            movieDao.getMovieById(901) shouldBeEqualTo null
            movieDao.getMovieById(902) shouldBeEqualTo null
            movieDao.getMovieById(903) shouldBeEqualTo null
        }
    }
}
