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

class MovieMetadataHttpTest {
    private val server = MockWebServer()
    private lateinit var dataSource: TmdbMovieMetadataDataSource

    @BeforeEach
    fun startServer() {
        server.start()
        val service =
            Retrofit
                .Builder()
                .baseUrl(server.url("/3/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MovieMetadataApiService::class.java)
        dataSource = TmdbMovieMetadataDataSource(service)
    }

    @AfterEach
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun `GIVEN detail JSON with co-directors and date WHEN requested THEN both cross the HTTP boundary`() =
        runTest {
            server.enqueue(MockResponse().setBody(detailJson(releaseDate = "\"1996-03-08\"", crew = COEN_CREW)))

            val result = dataSource.getMetadata(275)

            result.getOrThrow() shouldBeEqualTo
                RemoteMovieMetadata(
                    directors = listOf(Director(1223, "Joel Coen"), Director(1224, "Ethan Coen")),
                    releaseDate = "1996-03-08",
                )
            server.takeRequest().path shouldBeEqualTo "/3/movie/275?append_to_response=credits"
        }

    @Test
    fun `GIVEN detail JSON without director or date WHEN requested THEN both are resolved empty`() =
        runTest {
            server.enqueue(MockResponse().setBody(detailJson(releaseDate = "\"\"", crew = EDITOR_CREW)))

            dataSource.getMetadata(275).getOrThrow() shouldBeEqualTo RemoteMovieMetadata(emptyList(), "")
        }

    @Test
    fun `GIVEN detail JSON with null date WHEN requested THEN date is resolved empty`() =
        runTest {
            server.enqueue(MockResponse().setBody(detailJson(releaseDate = "null", crew = EDITOR_CREW)))

            dataSource.getMetadata(275).getOrThrow().releaseDate shouldBeEqualTo ""
        }

    @Test
    fun `GIVEN movie no longer exists WHEN requested THEN both are resolved empty`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))

            dataSource.getMetadata(275).getOrThrow() shouldBeEqualTo RemoteMovieMetadata(emptyList(), "")
        }

    @Test
    fun `GIVEN server error WHEN requested THEN failure is returned for retry`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500))

            dataSource.getMetadata(275).exceptionOrNull().shouldBeInstanceOf<HttpException>()
        }

    private fun detailJson(
        releaseDate: String,
        crew: String,
    ) = """
        {"id":275,"title":"Fargo","overview":"","poster_path":null,"backdrop_path":null,
         "release_date":$releaseDate,"credits":{"id":275,"cast":[],"crew":$crew}}
        """.trimIndent()

    private companion object {
        const val COEN_CREW =
            """[{"id":1223,"name":"Joel Coen","job":"Director"},""" +
                """{"id":1224,"name":"Ethan Coen","job":"Director"},""" +
                """{"id":151,"name":"Roger Deakins","job":"Director of Photography"}]"""
        const val EDITOR_CREW = """[{"id":151,"name":"Roger Deakins","job":"Editor"}]"""
    }
}
