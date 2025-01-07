package com.asensiodev.library.remoteconfig.impl

import com.asensiodev.core.testing.verifyOnce
import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FirebaseRemoteConfigProviderTest {
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var remoteConfigProvider: FirebaseRemoteConfigProvider

    @BeforeEach
    fun setUp() {
        firebaseRemoteConfig = mockk(relaxed = true)
        every { firebaseRemoteConfig.setConfigSettingsAsync(any<FirebaseRemoteConfigSettings>()) } returns mockk()
        every { firebaseRemoteConfig.fetchAndActivate() } returns mockk()
        remoteConfigProvider = FirebaseRemoteConfigProvider(firebaseRemoteConfig)
    }

    @Test
    fun `GIVEN init WHEN FirebaseRemoteConfigProvider initialized THEN config and fetchAndActivate are called`() {
        verifyOnce { firebaseRemoteConfig.setConfigSettingsAsync(any<FirebaseRemoteConfigSettings>()) }
        verifyOnce { firebaseRemoteConfig.fetchAndActivate() }
    }

    @Test
    fun `GIVEN string parameter WHEN getStringParameter THEN return expected string value`() {
        val testName = "TEST_STRING"
        val expectedValue = "test_string"
        val configName: RemoteConfigName =
            mockk {
                every { name } returns testName
            }
        every { firebaseRemoteConfig.getString(testName) } returns expectedValue

        val result: String = remoteConfigProvider.getStringParameter(configName)

        result shouldBeEqualTo expectedValue
        verifyOnce { firebaseRemoteConfig.getString(testName) }
    }
}
