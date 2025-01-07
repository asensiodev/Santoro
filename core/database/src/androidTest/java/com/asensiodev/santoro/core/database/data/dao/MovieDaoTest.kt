package com.asensiodev.santoro.core.database.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.santoro.core.database.MockUtils
import com.asensiodev.santoro.core.database.data.SantoroRoomDatabase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
    fun insertOrUpdateMovie_thenGetMovieById() =
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

            assertNotNull(retrievedMovie)
            assertEquals("Matrix", retrievedMovie?.title)
            assertEquals(true, retrievedMovie?.isInWatchlist)
            assertEquals("""["Action","Sci-Fi"]""", retrievedMovie?.genres)
            assertEquals("""["USA","Australia"]""", retrievedMovie?.productionCountries)
        }

    @Test
    fun getWatchlistMovies_returnsOnlyMoviesInWatchlist() =
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

            assertEquals(2, watchlistMovies.size)
            val titles = watchlistMovies.map { it.title }
            assert(titles.contains("Film 1"))
            assert(titles.contains("Film 3"))
        }

    @Test
    fun getWatchedMovies_returnsOnlyWatched() =
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

            assertEquals(2, watchedMovies.size)
            val titles = watchedMovies.map { it.title }
            assert(titles.contains("Watched 1"))
            assert(titles.contains("Watched 2"))
        }

    @ExperimentalCoroutinesApi
    @Test
    fun searchWatchedMoviesByTitle_returnsFilteredResults() =
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

            assertEquals(2, foundMovies.size)
            assert(foundMovies.any { it.title == "Matrix Reloaded" })
            assert(foundMovies.any { it.title == "Matrix Revolutions" })
        }

    @ExperimentalCoroutinesApi
    @Test
    fun updateMovie_updatesFieldsCorrectly() =
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
            assertNotNull(fromDb)
            assertEquals("New Title", fromDb?.title)
            assertEquals(true, fromDb?.isWatched)
            assertEquals("""["Action","Thriller"]""", fromDb?.genres)
            assertEquals("""["USA","Canada"]""", fromDb?.productionCountries)
        }
}
