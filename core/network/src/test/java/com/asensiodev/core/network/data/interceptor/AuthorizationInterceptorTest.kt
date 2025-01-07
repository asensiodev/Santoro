package com.asensiodev.core.network.data.interceptor

import com.asensiodev.core.network.data.ApiKeyProvider
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
        val apiKeyProvider =
            mockk<ApiKeyProvider> {
                every { getApiKey() } returns apiKey
            }

        val request = Request.Builder().url("https://example.com").build()
        val chain =
            mockk<Interceptor.Chain> {
                every { request() } returns request
                every { proceed(any()) } returns mockk<Response>()
            }

        val slot = slot<Request>()
        every { chain.proceed(capture(slot)) } answers { mockk() }

        val interceptor = AuthorizationInterceptor(apiKeyProvider)

        interceptor.intercept(chain)

        slot.captured.header("Authorization") shouldBeEqualTo "Bearer $apiKey"
        slot.captured.header("accept") shouldBeEqualTo "application/json"
    }

    @Test
    fun `GIVEN a null API key WHEN intercept is called THEN throws an IllegalStateException`() {
        val apiKeyProvider =
            mockk<ApiKeyProvider> {
                every { getApiKey() } returns null
            }

        val interceptor = AuthorizationInterceptor(apiKeyProvider)

        val chain = mockk<Interceptor.Chain>()

        assertThrows<IllegalStateException> {
            interceptor.intercept(chain)
        }
    }
}
