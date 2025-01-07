package com.asensiodev.core.network.data

import com.asensiodev.core.testing.verifyOnce
import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class RemoteConfigApiKeyProviderTest {
    @Test
    fun `GIVEN a RemoteConfigProvider WHEN getApiKey is called THEN returns expected API key`() {
        val expectedApiKey = "test_api_key"
        val remoteConfigProvider =
            mockk<RemoteConfigProvider> {
                every { getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY) } returns expectedApiKey
            }

        val apiKeyProvider = RemoteConfigApiKeyProvider(remoteConfigProvider)

        val result = apiKeyProvider.getApiKey()

        result shouldBeEqualTo expectedApiKey
        verifyOnce { remoteConfigProvider.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY) }
    }
}
