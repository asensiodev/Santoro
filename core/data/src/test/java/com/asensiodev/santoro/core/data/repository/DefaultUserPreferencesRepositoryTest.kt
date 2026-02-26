package com.asensiodev.santoro.core.data.repository

import com.asensiodev.core.domain.model.ThemeOption
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
        private const val KEY_ONBOARDING = "has_seen_guest_onboarding"
        private const val KEY_THEME = "theme_option"
    }

    @Test
    fun `GIVEN store returns true WHEN init THEN flow emits true`() =
        runTest {
            every { secureKeyValueStore.readString(KEY_ONBOARDING) } returns "true"

            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.hasSeenGuestOnboarding.first() shouldBeEqualTo true
            verify { secureKeyValueStore.readString(KEY_ONBOARDING) }
        }

    @Test
    fun `GIVEN store returns false WHEN init THEN flow emits false`() =
        runTest {
            every { secureKeyValueStore.readString(KEY_ONBOARDING) } returns "false"

            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.hasSeenGuestOnboarding.first() shouldBeEqualTo false
        }

    @Test
    fun `GIVEN store returns null WHEN init THEN flow emits false`() =
        runTest {
            every { secureKeyValueStore.readString(KEY_ONBOARDING) } returns null

            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.hasSeenGuestOnboarding.first() shouldBeEqualTo false
        }

    @Test
    fun `GIVEN initialized repository WHEN setHasSeenGuestOnboarding true THEN updates store and flow`() =
        runTest {
            every { secureKeyValueStore.readString(KEY_ONBOARDING) } returns "false"
            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.setHasSeenGuestOnboarding(true)

            verify { secureKeyValueStore.writeString(KEY_ONBOARDING, "true") }
            repository.hasSeenGuestOnboarding.first() shouldBeEqualTo true
        }

    @Test
    fun `GIVEN initialized repository WHEN setHasSeenGuestOnboarding false THEN updates store and flow`() =
        runTest {
            every { secureKeyValueStore.readString(KEY_ONBOARDING) } returns "true"
            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.setHasSeenGuestOnboarding(false)

            verify { secureKeyValueStore.writeString(KEY_ONBOARDING, "false") }
            repository.hasSeenGuestOnboarding.first() shouldBeEqualTo false
        }

    @Test
    fun `GIVEN no stored theme WHEN init THEN theme flow emits SYSTEM`() =
        runTest {
            every { secureKeyValueStore.readString(KEY_THEME) } returns null

            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.theme.first() shouldBeEqualTo ThemeOption.SYSTEM
        }

    @Test
    fun `GIVEN stored DARK WHEN init THEN theme flow emits DARK`() =
        runTest {
            every { secureKeyValueStore.readString(KEY_THEME) } returns "DARK"

            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.theme.first() shouldBeEqualTo ThemeOption.DARK
        }

    @Test
    fun `GIVEN repository WHEN setTheme LIGHT THEN writes LIGHT and emits LIGHT`() =
        runTest {
            repository = DefaultUserPreferencesRepository(secureKeyValueStore)

            repository.setTheme(ThemeOption.LIGHT)

            verify { secureKeyValueStore.writeString(KEY_THEME, "LIGHT") }
            repository.theme.first() shouldBeEqualTo ThemeOption.LIGHT
        }
}
