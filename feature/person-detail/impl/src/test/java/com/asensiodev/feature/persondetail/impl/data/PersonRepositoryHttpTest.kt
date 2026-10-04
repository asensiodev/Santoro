package com.asensiodev.feature.persondetail.impl.data

import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
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

class PersonRepositoryHttpTest {
    private val server = MockWebServer()
    private lateinit var repository: DefaultPersonRepository

    @BeforeEach
    fun startServer() {
        server.start()
        val service =
            Retrofit
                .Builder()
                .baseUrl(server.url("/3/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PersonApiService::class.java)
        repository = DefaultPersonRepository(service, TestDispatcherProvider())
    }

    @AfterEach
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun `GIVEN director profile JSON WHEN loaded THEN specialty crosses the HTTP boundary`() =
        runTest {
            server.enqueue(
                MockResponse().setBody("""{"id":7,"name":"Director","known_for_department":" Directing "}"""),
            )
            repository.getPerson(7).getOrThrow().knownForDepartment shouldBeEqualTo "Directing"
        }

    @Test
    fun `GIVEN complete profile JSON WHEN loaded THEN every personal fact crosses the HTTP boundary`() =
        runTest {
            server.enqueue(
                MockResponse().setBody(
                    """
                    {"id":7,"name":"Alex Morgan","biography":"  Biography  ","profile_path":"/alex.jpg",
                     "birthday":"1970-05-12","deathday":"2020-10-01","place_of_birth":" Madrid "}
                    """.trimIndent(),
                ),
            )
            repository.getPerson(7).getOrThrow() shouldBeEqualTo
                Person(7, "Alex Morgan", "Biography", "/alex.jpg", "1970-05-12", "2020-10-01", "Madrid")
        }

    @Test
    fun `GIVEN multi-role movie credits JSON WHEN loaded THEN dated movies group roles in both sections`() =
        runTest {
            server.enqueue(
                MockResponse().setBody(
                    """
                    {"cast":[
                      {"id":42,"title":"Shared Movie","poster_path":"/movie.jpg",
                       "release_date":"2024-03-15","character":"Lead"},
                      {"id":42,"title":"Shared Movie","character":"Narrator"},
                      {"id":12,"title":"Undated Movie","release_date":"","character":null}],
                     "crew":[
                      {"id":42,"title":"Shared Movie","poster_path":"/movie.jpg",
                       "release_date":"2024-03-15","job":"Director"},
                      {"id":42,"title":"Shared Movie","job":"Writer"}]}
                    """.trimIndent(),
                ),
            )
            repository.getMovieCredits(7).getOrThrow() shouldBeEqualTo
                PersonFilmography(
                    acting =
                        listOf(
                            PersonMovieCredit(
                                42,
                                "Shared Movie",
                                "/movie.jpg",
                                "2024-03-15",
                                listOf("Lead", "Narrator"),
                            ),
                            PersonMovieCredit(12, "Undated Movie", null, null, emptyList()),
                        ),
                    crew =
                        listOf(
                            PersonMovieCredit(
                                42,
                                "Shared Movie",
                                "/movie.jpg",
                                "2024-03-15",
                                listOf("Director", "Writer"),
                            ),
                        ),
                )
        }

    @Test
    fun `GIVEN unavailable profile WHEN retried after recovery THEN failure is replaced with the requested person`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))
            server.enqueue(MockResponse().setBody("""{"id":7,"name":"Recovered"}"""))
            val failure = repository.getPerson(7).exceptionOrNull()
            failure.shouldBeInstanceOf<HttpException>()
            (failure as HttpException).code() shouldBeEqualTo 404
            repository.getPerson(7).getOrThrow() shouldBeEqualTo Person(7, "Recovered", "", null, null, null, null)
        }

    @Test
    fun `GIVEN unavailable credits WHEN retried after recovery THEN empty filmography is returned`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(503))
            server.enqueue(MockResponse().setBody("""{"cast":null,"crew":null}"""))
            repository.getMovieCredits(7).exceptionOrNull().shouldBeInstanceOf<HttpException>()
            repository.getMovieCredits(7).getOrThrow() shouldBeEqualTo PersonFilmography(emptyList(), emptyList())
        }

    @Test
    fun `GIVEN wrong person identity in HTTP response WHEN loaded THEN another profile is never returned`() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"id":8,"name":"Another Person"}"""))
            repository.getPerson(7).exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }

    @Test
    fun `GIVEN malformed profile JSON WHEN loaded THEN decoding failure stays a retryable result`() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"id":7,"name": """))
            repository.getPerson(7).isFailure shouldBeEqualTo true
        }
}
