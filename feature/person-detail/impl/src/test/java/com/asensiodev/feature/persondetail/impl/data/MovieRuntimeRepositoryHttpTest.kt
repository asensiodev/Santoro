package com.asensiodev.feature.persondetail.impl.data

import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap

class MovieRuntimeRepositoryHttpTest {
    private val server = MockWebServer()
    private val responses = ConcurrentHashMap<String, MockResponse>()
    private lateinit var repository: TmdbMovieRuntimeRepository

    @BeforeEach
    fun startServer() {
        server.dispatcher =
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse =
                    responses[request.path.orEmpty()] ?: MockResponse().setResponseCode(NOT_FOUND)
            }
        server.start()
        val service =
            Retrofit
                .Builder()
                .baseUrl(server.url("/3/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MovieRuntimeApiService::class.java)
        repository = TmdbMovieRuntimeRepository(service, TestDispatcherProvider())
    }

    @AfterEach
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun `GIVEN movie details WHEN runtimes are requested THEN only known runtimes are returned`() =
        runTest {
            respond(1, """{"id":1,"runtime":30}""")
            respond(2, """{"id":2,"runtime":113}""")
            respond(3, """{"id":3,"runtime":0}""")
            respond(4, """{"id":4,"runtime":null}""")
            responses["/3/movie/5"] = MockResponse().setResponseCode(SERVER_ERROR)

            repository.getRuntimes(listOf(1, 2, 3, 4, 5)) shouldBeEqualTo mapOf(1 to 30, 2 to 113)
        }

    @Test
    fun `GIVEN loaded runtimes WHEN requested again THEN cached ones are not fetched and failed ones are retried`() =
        runTest {
            respond(1, """{"id":1,"runtime":30}""")
            responses["/3/movie/2"] = MockResponse().setResponseCode(SERVER_ERROR)
            repository.getRuntimes(listOf(1, 2))

            respond(2, """{"id":2,"runtime":113}""")
            val runtimes = repository.getRuntimes(listOf(1, 2))

            runtimes shouldBeEqualTo mapOf(1 to 30, 2 to 113)
            server.requestCount shouldBeEqualTo 3
        }

    private fun respond(
        id: Int,
        body: String,
    ) {
        responses["/3/movie/$id"] = MockResponse().setBody(body)
    }

    private companion object {
        const val NOT_FOUND = 404
        const val SERVER_ERROR = 500
    }
}
