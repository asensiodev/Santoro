package com.asensiodev.core.network.data.auth

import com.asensiodev.core.network.data.repository.ApiKeyRepository
import com.asensiodev.core.network.init.ApiKeyRefresher
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApiKeyAuthenticatorTest {
    private var refresher: ApiKeyRefresher = mockk()
    private var repository: ApiKeyRepository = mockk()
    private val request = Request.Builder().url("https://example.com").build()

    private lateinit var response: Response
    private lateinit var authenticator: ApiKeyAuthenticator

    @BeforeEach
    fun setUp() {
        response =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .message("Unauthorized")
                .code(401)
                .build()

        authenticator = ApiKeyAuthenticator(refresher, repository)
    }

    @Test
    fun `GIVEN no key cached and refresher succeeds WHEN authenticate THEN return new request with Authorization`() {
        coEvery { refresher.ensureKeyUpToDate() } just runs
        every { repository.getSyncOrNull() } returns "new-key"

        val newRequest = authenticator.authenticate(null, response)

        newRequest shouldNotBe null
        newRequest!!.header("Authorization") shouldBeEqualTo "Bearer new-key"
    }

    @Test
    fun `GIVEN refresher fails WHEN authenticate is called THEN return null`() {
        coEvery { refresher.ensureKeyUpToDate() } throws RuntimeException("network error")
        every { repository.getSyncOrNull() } returns null

        val newRequest = authenticator.authenticate(null, response)

        newRequest shouldBe null
    }

    @Test
    fun `GIVEN request was already retried once WHEN authenticate is called THEN do not retry again`() {
        coEvery { refresher.ensureKeyUpToDate() } just runs
        every { repository.getSyncOrNull() } returns "cached-key"

        val responseWithPrior =
            response
                .newBuilder()
                .priorResponse(response)
                .build()

        val newRequest = authenticator.authenticate(null, responseWithPrior)

        newRequest shouldBe null
    }

    @Test
    fun `GIVEN refresher succeeds but repository still blank WHEN authenticate is called THEN return null`() {
        coEvery { refresher.ensureKeyUpToDate() } just runs
        every { repository.getSyncOrNull() } returns null

        val newRequest = authenticator.authenticate(null, response)

        newRequest shouldBe null
    }
}
