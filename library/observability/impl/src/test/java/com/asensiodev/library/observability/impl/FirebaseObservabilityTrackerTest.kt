package com.asensiodev.library.observability.impl

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class FirebaseObservabilityTrackerTest {
    private val analytics: FirebaseAnalytics = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val sut = FirebaseObservabilityTracker(analytics, crashlytics)

    @Test
    fun `GIVEN authenticated category WHEN setting user THEN Firebase receives no identifier`() {
        sut.setUser(isAnonymous = false)

        verify(exactly = 1) { analytics.setUserId(null) }
        verify(exactly = 1) { crashlytics.setUserId("") }
        verify(exactly = 1) { analytics.setUserProperty("user_type", "registered") }
        verify(exactly = 1) { crashlytics.setCustomKey("user_type", "registered") }
    }

    @Test
    fun `GIVEN user state WHEN clearing THEN Firebase identifiers and category are cleared`() {
        sut.clearUser()

        verify(exactly = 1) { analytics.setUserId(null) }
        verify(exactly = 1) { crashlytics.setUserId("") }
        verify(exactly = 1) { analytics.setUserProperty("user_type", null) }
        verify(exactly = 1) { crashlytics.setCustomKey("user_type", "unauthenticated") }
    }

    @Test
    fun `GIVEN sensitive throwable WHEN recording error THEN Crashlytics receives sanitized exception`() {
        val sensitiveCause = IllegalArgumentException("secret cause identifier uid-123")
        val original = IllegalStateException("secret SDK message account@example.com", sensitiveCause)
        val recorded = slot<Throwable>()

        sut.recordError(
            errorName = "sync_failure",
            throwable = original,
            parameters = mapOf("action" to "download"),
        )

        verify(exactly = 1) { crashlytics.setCustomKey("error_name", "sync_failure") }
        verify(exactly = 1) { crashlytics.setCustomKey("action", "download") }
        verify(exactly = 1) { crashlytics.recordException(capture(recorded)) }
        recorded.captured shouldNotBeEqualTo original
        recorded.captured.message shouldBeEqualTo "Sanitized application error"
        recorded.captured.cause shouldBeEqualTo null
        recorded.captured.toString().contains(original.message.orEmpty()) shouldBeEqualTo false
        recorded.captured.toString().contains(sensitiveCause.message.orEmpty()) shouldBeEqualTo false
    }
}
