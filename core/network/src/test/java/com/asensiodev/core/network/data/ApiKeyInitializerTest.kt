package com.asensiodev.core.network.data

import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.internal.assertFailsWith
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.amshove.kluent.withMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ApiKeyInitializerTest {
    @MockK
    private lateinit var remoteConfigProvider: RemoteConfigProvider

    private lateinit var initializer: ApiKeyInitializer

    @BeforeEach
    fun setUp() {
        initializer = ApiKeyInitializer(remoteConfigProvider)
    }

    @Test
    fun `GIVEN remote config returns value WHEN initialize THEN caches apiKey`() =
        runTest {
            every {
                remoteConfigProvider.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
            } returns "test-api-key"

            initializer.initialize()

            initializer.getCachedApiKey() shouldBeEqualTo "test-api-key"
        }

    @Test
    fun `GIVEN remote config returns blank WHEN initialize THEN throws error`() =
        runTest {
            every {
                remoteConfigProvider.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
            } returns ""

            val exception =
                assertFailsWith<IllegalStateException> {
                    initializer.initialize()
                }

            exception.message shouldBeEqualTo "API key is missing or blank!"
        }

    @Test
    fun `GIVEN not initialized WHEN getCachedApiKey THEN throws error`() {
        invoking {
            initializer.getCachedApiKey()
        } shouldThrow IllegalStateException::class withMessage
            "API key not initialized. Did you forget to call initialize()?"
    }
}
