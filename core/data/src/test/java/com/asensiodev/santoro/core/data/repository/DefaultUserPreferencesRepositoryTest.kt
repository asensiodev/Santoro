package com.asensiodev.santoro.core.data.repository

import com.asensiodev.library.securestorage.api.SecureKeyValueStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class DefaultUserPreferencesRepositoryTest {
    private val secureKeyValueStore: SecureKeyValueStore = mockk(relaxed = true)

    private lateinit var repository: DefaultUserPreferencesRepository

    companion object {
        private const val KEY = "has_seen_guest_onboarding"
    }

    @Test
    fun `GIVEN store returns true WHEN init THEN flow emits true`() =
        runTest {
            every { secureKeyValueStore.readString(KEY) } returns "true"

            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.hasSeenGuestOnboarding.first() shouldBeEqualTo true
            verify { secureKeyValueStore.readString(KEY) }
        }

    @Test
    fun `GIVEN store returns false WHEN init THEN flow emits false`() =
        runTest {
            every { secureKeyValueStore.readString(KEY) } returns "false"

            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.hasSeenGuestOnboarding.first() shouldBeEqualTo false
        }

    @Test
    fun `GIVEN store returns null WHEN init THEN flow emits false`() =
        runTest {
            every { secureKeyValueStore.readString(KEY) } returns null

            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.hasSeenGuestOnboarding.first() shouldBeEqualTo false
        }

    @Test
    fun `GIVEN initialized repository WHEN setHasSeenGuestOnboarding true THEN updates store and flow`() =
        runTest {
            every { secureKeyValueStore.readString(KEY) } returns "false"
            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.setHasSeenGuestOnboarding(true)

            verify { secureKeyValueStore.writeString(KEY, "true") }
            repository.hasSeenGuestOnboarding.first() shouldBeEqualTo true
        }

    @Test
    fun `GIVEN initialized repository WHEN setHasSeenGuestOnboarding false THEN updates store and flow`() =
        runTest {
            every { secureKeyValueStore.readString(KEY) } returns "true"
            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.setHasSeenGuestOnboarding(false)

            verify { secureKeyValueStore.writeString(KEY, "false") }
            repository.hasSeenGuestOnboarding.first() shouldBeEqualTo false
        }
}
