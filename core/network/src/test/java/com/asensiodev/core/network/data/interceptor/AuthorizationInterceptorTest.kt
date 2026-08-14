package com.asensiodev.core.network.data.interceptor

import com.asensiodev.core.network.api.ApiKeyProviderContract
import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorizationInterceptorTest {
    private val apiKeyProvider: ApiKeyProviderContract = mockk()
    private lateinit var server: MockWebServer

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()
        server.enqueue(MockResponse().setResponseCode(200))
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `GIVEN missing API key WHEN request proceeds THEN authorization is absent and JSON is accepted`() {
        every { apiKeyProvider.getApiKey() } returns null

        executeRequest()

        val recorded = server.takeRequest()
        recorded.headers.values(AUTHORIZATION_HEADER).isEmpty() shouldBeEqualTo true
        recorded.headers.values(ACCEPT_HEADER) shouldBeEqualTo listOf(JSON_MEDIA_TYPE)
    }

    @Test
    fun `GIVEN blank API key WHEN request proceeds THEN authorization is absent`() {
        every { apiKeyProvider.getApiKey() } returns "   "

        executeRequest()

        val values = server.takeRequest().headers.values(AUTHORIZATION_HEADER)
        values.isEmpty() shouldBeEqualTo true
    }

    @Test
    fun `GIVEN existing authorization WHEN request proceeds THEN API key replaces it once`() {
        every { apiKeyProvider.getApiKey() } returns TEST_API_KEY

        executeRequest(existingAuthorization = true)

        val values = server.takeRequest().headers.values(AUTHORIZATION_HEADER)
        values.size shouldBeEqualTo 1
        (values.single() == "Bearer $TEST_API_KEY") shouldBeEqualTo true
    }

    private fun executeRequest(existingAuthorization: Boolean = false) {
        val requestBuilder = Request.Builder().url(server.url("/movies"))
        if (existingAuthorization) {
            requestBuilder.header(AUTHORIZATION_HEADER, "Bearer stale-test-value")
        }
        OkHttpClient
            .Builder()
            .addInterceptor(AuthorizationInterceptor(apiKeyProvider))
            .build()
            .newCall(requestBuilder.build())
            .execute()
            .use { response -> response.code shouldBeEqualTo 200 }
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val ACCEPT_HEADER = "Accept"
        const val JSON_MEDIA_TYPE = "application/json"
        const val TEST_API_KEY = "phase-five-synthetic-key"
    }
}
