package com.asensiodev.core.network.data

import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CachedApiKeyProviderTest {
    private val remoteConfigProvider = mockk<RemoteConfigProvider>()
    private lateinit var initializer: ApiKeyInitializer
    private lateinit var apiKeyProvider: CachedApiKeyProvider

    @BeforeEach
    fun setUp() {
        initializer = ApiKeyInitializer(remoteConfigProvider)
        apiKeyProvider = CachedApiKeyProvider(initializer)
    }

    @Test
    fun `GIVEN RemoteConfig returns value WHEN initialize THEN getApiKey returns that value`() =
        runTest {
            val expectedKey = "test_api_key"

            coEvery {
                remoteConfigProvider.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
            } returns expectedKey

            initializer.initialize()

            val result = apiKeyProvider.getApiKey()

            result shouldBeEqualTo expectedKey
            coVerify(exactly = 1) {
                remoteConfigProvider.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
            }
        }
}
