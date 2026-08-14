package com.asensiodev.core.network.data.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

class LanguageInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var previousLocale: Locale

    @BeforeEach
    fun setUp() {
        previousLocale = Locale.getDefault()
        server = MockWebServer()
        server.start()
        server.enqueue(MockResponse().setResponseCode(200))
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(previousLocale)
        server.shutdown()
    }

    @Test
    fun `GIVEN locale with country WHEN request proceeds THEN language includes country`() {
        Locale.setDefault(locale("en", "US"))

        val request = executeRequest()

        request.requestUrl?.queryParameterValues(LANGUAGE_PARAM) shouldBeEqualTo listOf("en-US")
    }

    @Test
    fun `GIVEN locale without country WHEN request proceeds THEN language contains language only`() {
        Locale.setDefault(locale("es"))

        val request = executeRequest()

        request.requestUrl?.queryParameterValues(LANGUAGE_PARAM) shouldBeEqualTo listOf("es")
    }

    @Test
    fun `GIVEN existing language WHEN request proceeds THEN app locale replaces it once`() {
        Locale.setDefault(locale("es", "ES"))

        val request = executeRequest("/movies?language=existing")

        request.requestUrl?.queryParameterValues(LANGUAGE_PARAM) shouldBeEqualTo listOf("es-ES")
    }

    private fun executeRequest(path: String = "/movies") =
        OkHttpClient
            .Builder()
            .addInterceptor(LanguageInterceptor())
            .build()
            .newCall(Request.Builder().url(server.url(path)).build())
            .execute()
            .use { response ->
                response.code shouldBeEqualTo 200
                server.takeRequest()
            }

    private fun locale(
        language: String,
        region: String? = null,
    ): Locale =
        Locale
            .Builder()
            .setLanguage(language)
            .apply {
                region?.let(::setRegion)
            }.build()

    private companion object {
        const val LANGUAGE_PARAM = "language"
    }
}
