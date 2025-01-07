package com.asensiodev.core.network.data

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ApiKeyProviderTest {
    @Test
    fun `GIVEN an ApiKeyProvider implementation WHEN getApiKey is called THEN it returns the expected API key`() {
        val expectedApiKey = "test_api_key"
        val apiKeyProvider =
            object : ApiKeyProvider {
                override fun getApiKey(): String = expectedApiKey
            }

        val result = apiKeyProvider.getApiKey()

        result shouldBeEqualTo expectedApiKey
    }
}
