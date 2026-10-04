package com.asensiodev.feature.persondetail.impl.data

import com.asensiodev.core.network.data.interceptor.LanguageInterceptor
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class PersonApiServiceTest {
    @Test
    fun `GIVEN Spanish locale WHEN profile and movie credits are requested THEN correct paths and language are used`() =
        runTest {
            val previous = Locale.getDefault()
            val server = MockWebServer()
            try {
                Locale.setDefault(Locale.forLanguageTag("es-ES"))
                server.start()
                server.enqueue(
                    MockResponse().setBody(
                        """
                        {"id":7,"name":"Persona","biography":null,"birthday":null,
                        "deathday":null,"place_of_birth":null,"profile_path":null}
                        """.trimIndent(),
                    ),
                )
                server.enqueue(MockResponse().setBody("""{"cast":[],"crew":[]}"""))
                val service =
                    Retrofit
                        .Builder()
                        .baseUrl(server.url("/3/"))
                        .client(OkHttpClient.Builder().addInterceptor(LanguageInterceptor()).build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(PersonApiService::class.java)
                service.person(7).toDomain().name shouldBeEqualTo "Persona"
                service.movieCredits(7).toDomain().acting shouldBeEqualTo emptyList()
                val details = server.takeRequest().requestUrl
                val credits = server.takeRequest().requestUrl
                details?.encodedPath shouldBeEqualTo "/3/person/7"
                credits?.encodedPath shouldBeEqualTo "/3/person/7/movie_credits"
                details?.queryParameterValues("language") shouldBeEqualTo listOf("es-ES")
                credits?.queryParameterValues("language") shouldBeEqualTo listOf("es-ES")
            } finally {
                Locale.setDefault(previous)
                server.shutdown()
            }
        }
}
