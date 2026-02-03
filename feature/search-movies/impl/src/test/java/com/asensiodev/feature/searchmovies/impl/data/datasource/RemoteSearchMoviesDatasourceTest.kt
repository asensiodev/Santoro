import com.asensiodev.core.domain.Result
import com.asensiodev.feature.searchmovies.impl.data.datasource.RemoteSearchMoviesDatasource
import com.asensiodev.feature.searchmovies.impl.data.model.SearchMoviesResponseApiModel
import com.asensiodev.feature.searchmovies.impl.data.service.SearchMoviesApiService
import com.asensiodev.santoro.core.data.mapper.toDomain
import com.asensiodev.santoro.core.data.model.MovieApiModel
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

class RemoteSearchMoviesDatasourceTest {
    private val apiService: SearchMoviesApiService = mockk()

    private lateinit var datasource: RemoteSearchMoviesDatasource

    @BeforeEach
    fun setUp() {
        datasource = RemoteSearchMoviesDatasource(apiService)
    }

    @Test
    fun `GIVEN success response WHEN searchMovies THEN returns expected movies`() =
        runTest {
            val query = "Inception"
            val page = 1
            val apiResponse =
                SearchMoviesResponseApiModel(
                    results =
                        listOf(
                            MovieApiModel(
                                id = 1,
                                title = "Inception",
                                overview = "A thief who steals corporate secrets through dream-sharing technology.",
                                releaseDate = "2010-07-16",
                                posterPath = "/inception.jpg",
                                backdropPath = null,
                                popularity = 8.3,
                                voteAverage = 8.8,
                                voteCount = 32000,
                                genres = listOf(),
                                genreIds = listOf(),
                                productionCountries = listOf(),
                                runtime = null,
                                credits = null,
                            ),
                        ),
                    page = 1,
                    totalPages = 10,
                    totalResults = 100,
                )
            val expectedMovies = apiResponse.results.map { it.toDomain() }
            coEvery { apiService.searchMovies(query, page) } returns apiResponse

            val result = datasource.searchMovies(query, page)

            (result as Result.Success).data shouldBeEqualTo expectedMovies
        }

    @Test
    fun `GIVEN IOException WHEN searchMovies THEN returns expected error`() =
        runTest {
            val query = "Inception"
            val page = 1
            val exceptionMessage = "Network error"
            coEvery { apiService.searchMovies(query, page) } throws IOException(exceptionMessage)

            val result = datasource.searchMovies(query, page)

            (result as Result.Error).exception.shouldBeInstanceOf<IOException>()
            result.exception.message shouldBeEqualTo exceptionMessage
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
            coEvery { apiService.searchMovies(query, page) } throws
                HttpException(
                    Response.error<ResponseBody>(404, responseBody),
                )

            val result = datasource.searchMovies(query, page)

            (result as Result.Error).exception.shouldBeInstanceOf<HttpException>()
        }

    @Test
    fun `GIVEN success response WHEN getPopularMovies THEN returns popular movies`() =
        runTest {
            val page = 1
            val apiResponse =
                SearchMoviesResponseApiModel(
                    results =
                        listOf(
                            MovieApiModel(
                                id = 2,
                                title = "The Dark Knight",
                                overview = "Batman faces the Joker in Gotham City.",
                                releaseDate = "2008-07-18",
                                posterPath = "/dark_knight.jpg",
                                backdropPath = null,
                                popularity = 9.0,
                                voteAverage = 9.1,
                                voteCount = 41000,
                                genres = listOf(),
                                genreIds = listOf(),
                                productionCountries = listOf(),
                                runtime = null,
                                credits = null,
                            ),
                        ),
                    page = 1,
                    totalPages = 10,
                    totalResults = 100,
                )
            val expectedMovies = apiResponse.results.map { it.toDomain() }
            coEvery { apiService.getPopularMovies(page) } returns apiResponse

            val result = datasource.getPopularMovies(page)

            (result as Result.Success).data shouldBeEqualTo expectedMovies
        }

    @Test
    fun `GIVEN IOException WHEN getPopularMovies THEN returns expected error`() =
        runTest {
            val page = 1
            val exceptionMessage = "Network error"
            coEvery { apiService.getPopularMovies(page) } throws IOException(exceptionMessage)

            val result = datasource.getPopularMovies(page)

            (result as Result.Error).exception.shouldBeInstanceOf<IOException>()
            result.exception.message shouldBeEqualTo exceptionMessage
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
            coEvery { apiService.getPopularMovies(page) } throws
                HttpException(
                    Response.error<ResponseBody>(404, responseBody),
                )

            val result = datasource.getPopularMovies(page)

            (result as Result.Error).exception.shouldBeInstanceOf<HttpException>()
        }
}
