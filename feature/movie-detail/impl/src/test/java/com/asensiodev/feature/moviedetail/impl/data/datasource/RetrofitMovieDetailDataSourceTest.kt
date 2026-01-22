package com.asensiodev.feature.moviedetail.impl.data.datasource

import app.cash.turbine.test
import com.asensiodev.core.domain.Result
import com.asensiodev.feature.moviedetail.impl.data.service.MovieDetailApiService
import com.asensiodev.santoro.core.data.mapper.toDomain
import com.asensiodev.santoro.core.data.model.MovieApiModel
import io.mockk.coEvery
import io.mockk.every
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

class RetrofitMovieDetailDataSourceTest {
    private val apiService: MovieDetailApiService = mockk()

    private lateinit var dataSource: RetrofitMovieDetailDataSource

    @BeforeEach
    fun setUp() {
        dataSource = RetrofitMovieDetailDataSource(apiService)
    }

    @Test
    fun `GIVEN success response WHEN getMovieDetail THEN returns the expected movie`() =
        runTest {
            val movieId = 1
            val movieApiModel =
                MovieApiModel(
                    id = movieId,
                    title = "Test Movie",
                    overview = "This is a test overview",
                    posterPath = null,
                    backdropPath = null,
                    releaseDate = null,
                    popularity = null,
                    voteAverage = null,
                    voteCount = null,
                    genres = listOf(),
                    productionCountries = listOf(),
                )
            val expectedDomainMovie = movieApiModel.toDomain()

            coEvery { apiService.movieDetail(movieId) } returns movieApiModel

            dataSource.getMovieDetail(movieId).test {
                awaitItem() shouldBeEqualTo Result.Success(expectedDomainMovie)
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN API IOException WHEN getMovieDetail THEN returns expected error`() =
        runTest {
            val movieId = 1
            val exceptionMessage = "Network error"

            coEvery { apiService.movieDetail(movieId) } throws IOException(exceptionMessage)

            dataSource.getMovieDetail(movieId).test {
                val error = awaitItem() as Result.Error
                error.exception.shouldBeInstanceOf<IOException>()
                error.exception.message shouldBeEqualTo exceptionMessage
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN API HttpException WHEN getMovieDetail THEN returns expected error`() =
        runTest {
            val movieId = 1
            val responseBody =
                mockk<ResponseBody> {
                    every { contentType() } returns null
                    every { contentLength() } returns 0L
                }

            coEvery { apiService.movieDetail(movieId) } throws
                HttpException(
                    Response.error<ResponseBody>(404, responseBody),
                )

            dataSource.getMovieDetail(movieId).test {
                val error = awaitItem() as Result.Error
                error.exception.shouldBeInstanceOf<HttpException>()
                awaitComplete()
            }
        }
}
