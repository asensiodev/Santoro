package com.asensiodev.core.network.data.repository

import com.asensiodev.core.network.data.ApiKeyStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ApiKeyRepositoryTest {
    private var storage: ApiKeyStorage = mockk(relaxed = true)

    private lateinit var repository: ApiKeyRepository

    @BeforeEach
    fun setUp() {
        repository = ApiKeyRepository(storage)
    }

    @Test
    fun `GIVEN refresh api key request WHEN getSyncOrNull is called THEN returns cached value if present`() {
        repository.refreshFromMemory("cached-key")

        val result = repository.getSyncOrNull()

        result shouldBeEqualTo "cached-key"
        verify(exactly = 0) { storage.read() }
    }

    @Test
    fun `GIVEN refresh api key request WHEN getSyncOrNull is called THEN reads from storage if cache is empty`() {
        every { storage.read() } returns "persisted-key"

        val result = repository.getSyncOrNull()

        result shouldBeEqualTo "persisted-key"
        repository.getSyncOrNull() shouldBeEqualTo "persisted-key"
        verify(exactly = 1) { storage.read() }
    }

    @Test
    fun `GIVEN refresh api key request WHEN refreshFromRemote is called THEN persists and updates cache`() {
        coEvery { storage.write(any()) } just runs

        runBlocking {
            repository.refreshFromRemote { "fresh-key" }
        }

        repository.getSyncOrNull() shouldBeEqualTo "fresh-key"
        coVerify { storage.write("fresh-key") }
    }

    @Test
    fun `GIVEN refresh api key request WHEN refreshFromRemote is called THEN throws if blank`() {
        assertThrows<IllegalArgumentException> {
            runBlocking {
                repository.refreshFromRemote { "" }
            }
        }
        verify(exactly = 0) { storage.write(any()) }
    }

    private fun ApiKeyRepository.refreshFromMemory(value: String) {
        val field = ApiKeyRepository::class.java.getDeclaredField("cached")
        field.isAccessible = true
        field.set(this, value)
    }
}
