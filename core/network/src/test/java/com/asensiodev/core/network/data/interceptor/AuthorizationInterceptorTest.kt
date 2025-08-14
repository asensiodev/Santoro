package com.asensiodev.core.network.data.interceptor

import com.asensiodev.core.network.api.ApiKeyProviderContract
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AuthorizationInterceptorTest {
    @Test
    fun `GIVEN a valid API key WHEN intercept is called THEN adds the Authorization header`() {
        val apiKey = "test_api_key"
        val apiKeyProviderContract =
            mockk<ApiKeyProviderContract> {
                every { getApiKey() } returns apiKey
            }

        val request = Request.Builder().url("https://example.com").build()
        val capturedRequest = slot<Request>()

        val chain =
            mockk<Interceptor.Chain> {
                every { request() } returns request
                every { proceed(capture(capturedRequest)) } returns mockk<Response>()
            }

        val interceptor = AuthorizationInterceptor(apiKeyProviderContract)

        interceptor.intercept(chain)

        capturedRequest.captured.header("Authorization") shouldBeEqualTo "Bearer $apiKey"
        capturedRequest.captured.header("accept") shouldBeEqualTo "application/json"
    }

    @Test
    fun `GIVEN ApiKeyProvider throws error WHEN intercept is called THEN throws exception`() {
        val apiKeyProviderContract =
            mockk<ApiKeyProviderContract> {
                every { getApiKey() } throws IllegalStateException("API key not initialized")
            }

        val request = Request.Builder().url("https://example.com").build()
        val chain =
            mockk<Interceptor.Chain> {
                every { request() } returns request
            }

        val interceptor = AuthorizationInterceptor(apiKeyProviderContract)

        val exception =
            assertThrows<IllegalStateException> {
                interceptor.intercept(chain)
            }

        exception.message shouldBeEqualTo "API key not initialized"
    }
}
