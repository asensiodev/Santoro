package com.asensiodev.feature.searchmovies.impl.data.repository

import app.cash.turbine.test
import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.data.datasource.SearchMoviesDatasource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RemoteSearchMoviesRepositoryTest {
    private val datasource: SearchMoviesDatasource = mockk()

    private lateinit var repository: RemoteSearchMoviesRepository

    @BeforeEach
    fun setUp() {
        repository = RemoteSearchMoviesRepository(datasource)
    }

    @Test
    fun `GIVEN success response WHEN searchMovies THEN returns expected movies`() =
        runTest {
            val query = "Inception"
            val page = 1
            val movies =
                listOf(
                    Movie(
                        id = 1,
                        title = "Inception",
                        overview = "A thief who steals corporate secrets through dream-sharing technology.",
                        releaseDate = "2010-07-16",
                        posterPath = "/inception.jpg",
                        popularity = 8.3,
                        voteAverage = 8.8,
                        voteCount = 32000,
                        genres = listOf(),
                        productionCountries = listOf(),
                        isWatched = false,
                        isInWatchlist = false,
                    ),
                )
            coEvery { datasource.searchMovies(query, page) } returns Result.Success(movies)

            repository.searchMovies(query, page).test {
                awaitItem() shouldBeEqualTo Result.Success(movies)
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN IOException WHEN searchMovies THEN returns expected error`() =
        runTest {
            val query = "Inception"
            val page = 1
            val exceptionMessage = "Network error"
            coEvery { datasource.searchMovies(query, page) } throws IOException(exceptionMessage)

            repository.searchMovies(query, page).test {
                val error = awaitItem() as Result.Error
                error.exception.shouldBeInstanceOf<IOException>()
                error.exception.message shouldBeEqualTo exceptionMessage
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN HttpException WHEN searchMovies THEN returns expected error`() =
        runTest {
            val query = "Inception"
            val page = 1
            val responseBody =
                mockk<ResponseBody> {
                    coEvery { contentType() } returns null
                    coEvery { contentLength() } returns 0L
                }
            coEvery { datasource.searchMovies(query, page) } throws
                HttpException(
                    Response.error<ResponseBody>(404, responseBody),
                )

            repository.searchMovies(query, page).test {
                val error = awaitItem() as Result.Error
                error.exception.shouldBeInstanceOf<HttpException>()
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN success response WHEN getPopularMovies THEN returns expected movies`() =
        runTest {
            val page = 1
            val movies =
                listOf(
                    Movie(
                        id = 2,
                        title = "The Dark Knight",
                        overview = "Batman faces the Joker in Gotham City.",
                        releaseDate = "2008-07-18",
                        posterPath = "/dark_knight.jpg",
                        popularity = 9.0,
                        voteAverage = 9.1,
                        voteCount = 41000,
                        genres = listOf(),
                        productionCountries = listOf(),
                        isWatched = false,
                        isInWatchlist = false,
                    ),
                )
            coEvery { datasource.getPopularMovies(page) } returns Result.Success(movies)

            repository.getPopularMovies(page).test {
                awaitItem() shouldBeEqualTo Result.Success(movies)
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN IOException WHEN getPopularMovies THEN returns expected error`() =
        runTest {
            val page = 1
            val exceptionMessage = "Network error"
            coEvery { datasource.getPopularMovies(page) } throws IOException(exceptionMessage)

            repository.getPopularMovies(page).test {
                val error = awaitItem() as Result.Error
                error.exception.shouldBeInstanceOf<IOException>()
                error.exception.message shouldBeEqualTo exceptionMessage
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN HttpException WHEN getPopularMovies THEN returns expected error`() =
        runTest {
            val page = 1
            val responseBody =
                mockk<ResponseBody> {
                    coEvery { contentType() } returns null
                    coEvery { contentLength() } returns 0L
                }
            coEvery { datasource.getPopularMovies(page) } throws
                HttpException(
                    Response.error<ResponseBody>(404, responseBody),
                )

            repository.getPopularMovies(page).test {
                val error = awaitItem() as Result.Error
                error.exception.shouldBeInstanceOf<HttpException>()
                awaitComplete()
            }
        }
}
