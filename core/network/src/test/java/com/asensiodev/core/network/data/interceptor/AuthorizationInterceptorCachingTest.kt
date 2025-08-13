package com.asensiodev.core.network.data.interceptor

import com.asensiodev.core.network.data.ApiKeyInitializer
import com.asensiodev.core.network.data.ApiKeyProviderContract
import com.asensiodev.core.network.data.CachedApiKeyProvider
import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorizationInterceptorCachingTest {
    private val remoteConfigProvider = mockk<RemoteConfigProvider>()
    private lateinit var initializer: ApiKeyInitializer
    private lateinit var apiKeyProviderContract: ApiKeyProviderContract
    private lateinit var interceptor: AuthorizationInterceptor

    @BeforeEach
    fun setUp() {
        initializer = ApiKeyInitializer(remoteConfigProvider)
        apiKeyProviderContract = CachedApiKeyProvider(initializer)
        interceptor = AuthorizationInterceptor(apiKeyProviderContract)
    }

    @Test
    fun `GIVEN initializer is called once WHEN multiple service calls THEN remote config is not called again`() =
        runTest {
            // GIVEN
            val expectedApiKey = "cached_key_123"
            coEvery {
                remoteConfigProvider.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
            } returns expectedApiKey

            initializer.initialize()

            val capturedRequest1 = slot<Request>()
            val capturedRequest2 = slot<Request>()

            val request = Request.Builder().url("https://example.com").build()

            val chain1 =
                mockk<Interceptor.Chain> {
                    every { request() } returns request
                    every { proceed(capture(capturedRequest1)) } returns mockk<Response>()
                }

            val chain2 =
                mockk<Interceptor.Chain> {
                    every { request() } returns request
                    every { proceed(capture(capturedRequest2)) } returns mockk<Response>()
                }

            // WHEN
            interceptor.intercept(chain1)
            interceptor.intercept(chain2)

            // THEN
            coVerify(exactly = 1) {
                remoteConfigProvider.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
            }

            capturedRequest1.captured.header("Authorization") shouldBeEqualTo "Bearer $expectedApiKey"
            capturedRequest2.captured.header("Authorization") shouldBeEqualTo "Bearer $expectedApiKey"
        }
}
