package com.asensiodev.core.network.di

import com.asensiodev.core.buildconfig.BuildConfig
import com.asensiodev.core.network.api.ApiKeyProviderContract
import com.asensiodev.core.network.data.auth.ApiKeyAuthenticator
import com.asensiodev.core.network.data.interceptor.AuthorizationInterceptor
import com.asensiodev.core.network.data.interceptor.LanguageInterceptor
import io.mockk.every
import io.mockk.mockk
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

class NetworkProvidesModuleTest {
    private val apiKeyProvider: ApiKeyProviderContract = mockk()
    private val authenticator: ApiKeyAuthenticator = mockk()
    private lateinit var server: MockWebServer
    private lateinit var previousLocale: Locale

    @BeforeEach
    fun setUp() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(
            Locale
                .Builder()
                .setLanguage("en")
                .setRegion("US")
                .build(),
        )
        server = MockWebServer()
        server.start()
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(previousLocale)
        server.shutdown()
    }

    @Test
    fun `GIVEN assembled client WHEN request executes THEN interceptors and request contract are preserved`() {
        every { apiKeyProvider.getApiKey() } returns TEST_API_KEY
        val logging = createLoggingInterceptor { }
        val client = NetworkProvidesModule.provideOkHttpClient(apiKeyProvider, logging, authenticator)

        client
            .newCall(Request.Builder().url(server.url("/movies")).build())
            .execute()
            .use { response -> response.code shouldBeEqualTo 200 }

        val interceptorTypes = client.interceptors.map { interceptor -> interceptor::class }
        interceptorTypes.take(2) shouldBeEqualTo
            listOf(AuthorizationInterceptor::class, LanguageInterceptor::class)
        interceptorTypes.contains(HttpLoggingInterceptor::class) shouldBeEqualTo BuildConfig.DEBUG
        (client.authenticator === authenticator) shouldBeEqualTo true
        val request = server.takeRequest()
        request.headers.values("Accept") shouldBeEqualTo listOf("application/json")
        request.requestUrl?.queryParameterValues("language") shouldBeEqualTo listOf("en-US")
        request.headers.values("Authorization").size shouldBeEqualTo 1
    }

    @Test
    fun `GIVEN debug logging WHEN authorized request executes THEN authorization is redacted`() {
        val logLines = mutableListOf<String>()
        val logging = createLoggingInterceptor { message -> logLines += message }
        val client =
            okhttp3.OkHttpClient
                .Builder()
                .addInterceptor(AuthorizationInterceptor(apiKeyProvider))
                .addInterceptor(logging)
                .build()
        every { apiKeyProvider.getApiKey() } returns TEST_API_KEY

        client
            .newCall(Request.Builder().url(server.url("/movies")).build())
            .execute()
            .use { response -> response.code shouldBeEqualTo 200 }

        logLines.any { line -> line.contains("Authorization: ") } shouldBeEqualTo true
        logLines.none { line -> line.contains(TEST_API_KEY) } shouldBeEqualTo true
    }

    private companion object {
        const val TEST_API_KEY = "phase-five-synthetic-key"
    }
}
