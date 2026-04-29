package com.asensiodev.library.remoteconfig.impl

import com.asensiodev.core.testing.coVerifyOnce
import com.asensiodev.core.testing.verifyOnce
import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
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
        remoteConfigProvider = FirebaseRemoteConfigProvider(firebaseRemoteConfig)
    }

    @Test
    fun `GIVEN init WHEN FirebaseRemoteConfigProvider initialized THEN config settings are set`() {
        verifyOnce { firebaseRemoteConfig.setConfigSettingsAsync(any<FirebaseRemoteConfigSettings>()) }
    }

    @Test
    fun `GIVEN initialize WHEN called THEN fetchAndActivate is awaited`() =
        runTest {
            val task = mockk<Task<Boolean>>()
            coEvery { task.await() } returns true
            every { firebaseRemoteConfig.fetchAndActivate() } returns task

            remoteConfigProvider.initialize()

            verifyOnce { firebaseRemoteConfig.fetchAndActivate() }
            coVerifyOnce { task.await() }
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
