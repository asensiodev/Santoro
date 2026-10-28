package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.core.domain.model.Director
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MovieDirectorsHttpTest {
    private val server = MockWebServer()
    private lateinit var dataSource: TmdbMovieDirectorsDataSource

    @BeforeEach
    fun startServer() {
        server.start()
        val service =
            Retrofit
                .Builder()
                .baseUrl(server.url("/3/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MovieCreditsApiService::class.java)
        dataSource = TmdbMovieDirectorsDataSource(service)
    }

    @AfterEach
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun `GIVEN credits JSON with co-directors WHEN requested THEN directors cross the HTTP boundary`() =
        runTest {
            server.enqueue(
                MockResponse().setBody(
                    """
                    {"id":115,"cast":[],"crew":[
                      {"id":1223,"name":"Joel Coen","job":"Director"},
                      {"id":1224,"name":"Ethan Coen","job":"Director"},
                      {"id":151,"name":"Roger Deakins","job":"Director of Photography"}]}
                    """.trimIndent(),
                ),
            )

            val result = dataSource.getDirectors(115)

            result.getOrThrow() shouldBeEqualTo
                listOf(Director(1223, "Joel Coen"), Director(1224, "Ethan Coen"))
            server.takeRequest().path shouldBeEqualTo "/3/movie/115/credits"
        }

    @Test
    fun `GIVEN credits JSON without a director WHEN requested THEN directors are resolved empty`() =
        runTest {
            server.enqueue(
                MockResponse().setBody(
                    """{"id":115,"cast":[],"crew":[{"id":151,"name":"Roger Deakins","job":"Editor"}]}""",
                ),
            )

            dataSource.getDirectors(115).getOrThrow() shouldBeEqualTo emptyList()
        }

    @Test
    fun `GIVEN movie no longer exists WHEN requested THEN directors are resolved empty`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))

            dataSource.getDirectors(115).getOrThrow() shouldBeEqualTo emptyList()
        }

    @Test
    fun `GIVEN server error WHEN requested THEN failure is returned for retry`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500))

            dataSource.getDirectors(115).exceptionOrNull().shouldBeInstanceOf<HttpException>()
        }
}
