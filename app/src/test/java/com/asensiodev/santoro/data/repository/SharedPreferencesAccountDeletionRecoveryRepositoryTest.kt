package com.asensiodev.santoro.data.repository

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SharedPreferencesAccountDeletionRecoveryRepositoryTest {
    private val preferences: SharedPreferences = mockk()
    private val editor: SharedPreferences.Editor = mockk()

    private lateinit var sut: SharedPreferencesAccountDeletionRecoveryRepository

    @BeforeEach
    fun setUp() {
        every { preferences.getBoolean(any(), false) } returns false
        every { preferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.commit() } returns true
        sut = SharedPreferencesAccountDeletionRecoveryRepository(preferences)
    }

    @Test
    fun `GIVEN no pending cleanup WHEN marked THEN persists before publishing pending state`() =
        runTest {
            val result = sut.markLocalCleanupPending()

            result.isSuccess shouldBeEqualTo true
            sut.isLocalCleanupPending.first() shouldBeEqualTo true
            sut.isRemoteDeletionInFlight.first() shouldBeEqualTo false
            verify(exactly = 1) { editor.putBoolean("local_cleanup_pending", true) }
            verify(exactly = 1) { editor.commit() }
        }

    @Test
    fun `GIVEN remote deletion completes WHEN lease is released THEN durable marker remains pending`() =
        runTest {
            sut.beginRemoteDeletion()
            sut.markLocalCleanupPending()

            sut.completeRemoteDeletion()

            sut.isRemoteDeletionInFlight.first() shouldBeEqualTo false
            sut.isLocalCleanupPending.first() shouldBeEqualTo true
        }

    @Test
    fun `GIVEN pending cleanup WHEN cleared THEN persists and publishes cleared state`() =
        runTest {
            sut.markLocalCleanupPending()

            val result = sut.clearLocalCleanupPending()

            result.isSuccess shouldBeEqualTo true
            sut.isLocalCleanupPending.first() shouldBeEqualTo false
            verify(exactly = 1) { editor.putBoolean("local_cleanup_pending", false) }
        }

    @Test
    fun `GIVEN persistence fails WHEN marked THEN returns failure without publishing pending state`() =
        runTest {
            every { editor.commit() } returns false

            val result = sut.markLocalCleanupPending()

            result.isFailure shouldBeEqualTo true
            sut.isLocalCleanupPending.first() shouldBeEqualTo false
        }

    @Test
    fun `GIVEN persisted marker WHEN repository starts THEN exposes pending state`() =
        runTest {
            every { preferences.getBoolean(any(), false) } returns true

            sut = SharedPreferencesAccountDeletionRecoveryRepository(preferences)

            sut.isLocalCleanupPending.first() shouldBeEqualTo true
        }
}
