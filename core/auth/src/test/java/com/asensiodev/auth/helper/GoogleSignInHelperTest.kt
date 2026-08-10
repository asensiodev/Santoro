package com.asensiodev.auth.helper

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.NoCredentialException
import com.asensiodev.santoro.core.stringresources.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class GoogleSignInHelperTest {
    private val applicationContext: Context = mockk()
    private val activityContext: Context = mockk()
    private val credentialManager: CredentialManager = mockk()
    private lateinit var sut: GoogleSignInHelper

    @Before
    fun setUp() {
        ShadowLog.clear()
        every { applicationContext.getString(R.string.default_web_client_id) } returns TEST_CLIENT_ID
        sut = GoogleSignInHelper(applicationContext, credentialManager)
    }

    @After
    fun tearDown() {
        val containsSensitiveValue =
            ShadowLog
                .getLogsForTag(GOOGLE_SIGN_IN_TAG)
                .any { log ->
                    log.msg.contains(TEST_ID_TOKEN) ||
                        log.msg.contains(TEST_CLIENT_ID) ||
                        log.throwable?.message?.contains(TEST_ID_TOKEN) == true ||
                        log.throwable?.message?.contains(TEST_CLIENT_ID) == true
                }
        containsSensitiveValue shouldBeEqualTo false
    }

    @Test
    fun `GIVEN primary Google credential WHEN signing in THEN token parses and request is configured`() =
        runTest {
            val requests = mutableListOf<GetCredentialRequest>()
            coEvery {
                credentialManager.getCredential(activityContext, capture(requests))
            } returns googleResponse()

            val result = sut.signIn(activityContext)

            result.getOrNull()?.sha256() shouldBeEqualTo TEST_ID_TOKEN.sha256()
            requests.size shouldBeEqualTo 1
            val option = requests.single().credentialOptions.single()
            option.shouldBeInstanceOf<GetGoogleIdOption>()
            val googleIdOption = option as GetGoogleIdOption
            googleIdOption.serverClientId shouldBeEqualTo TEST_CLIENT_ID
            googleIdOption.filterByAuthorizedAccounts shouldBeEqualTo false
            googleIdOption.autoSelectEnabled shouldBeEqualTo false
        }

    @Test
    fun `GIVEN no primary credential WHEN signing in THEN fallback request returns parsed token`() =
        runTest {
            val requests = mutableListOf<GetCredentialRequest>()
            var invocation = 0
            coEvery {
                credentialManager.getCredential(activityContext, capture(requests))
            } answers {
                if (invocation++ == 0) throw NoCredentialException()
                googleResponse()
            }

            val result = sut.signIn(activityContext)

            result.getOrNull()?.sha256() shouldBeEqualTo TEST_ID_TOKEN.sha256()
            requests.size shouldBeEqualTo 2
            requests[0].credentialOptions.single().shouldBeInstanceOf<GetGoogleIdOption>()
            val fallbackOption = requests[1].credentialOptions.single()
            fallbackOption.shouldBeInstanceOf<GetSignInWithGoogleOption>()
            (fallbackOption as GetSignInWithGoogleOption).serverClientId shouldBeEqualTo TEST_CLIENT_ID
        }

    @Test
    fun `GIVEN unsupported credential type WHEN signing in THEN credential is rejected`() =
        runTest {
            val googleCredential = googleCredential()
            val unsupportedCredential =
                CustomCredential(
                    type = "com.example.test.UNSUPPORTED_CREDENTIAL",
                    data = googleCredential.data,
                )
            coEvery {
                credentialManager.getCredential(activityContext, any<GetCredentialRequest>())
            } returns GetCredentialResponse(unsupportedCredential)

            sut.signIn(activityContext).isFailure shouldBeEqualTo true
        }

    @Test
    fun `GIVEN coroutine cancellation WHEN signing in THEN cancellation propagates`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            coEvery {
                credentialManager.getCredential(activityContext, any<GetCredentialRequest>())
            } throws cancellation

            captureCancellation { sut.signIn(activityContext) } shouldBeEqualTo cancellation
            coVerify(exactly = 1) {
                credentialManager.getCredential(activityContext, any<GetCredentialRequest>())
            }
        }

    @Test
    fun `GIVEN credential manager failure WHEN signing in THEN failure is returned without fallback`() =
        runTest {
            val failure = GetCredentialUnknownException("Synthetic credential manager failure")
            coEvery {
                credentialManager.getCredential(activityContext, any<GetCredentialRequest>())
            } throws failure

            sut.signIn(activityContext).exceptionOrNull() shouldBeEqualTo failure
            coVerify(exactly = 1) {
                credentialManager.getCredential(activityContext, any<GetCredentialRequest>())
            }
        }

    @Test
    fun `GIVEN unexpected credential failure WHEN signing in THEN failure is returned`() =
        runTest {
            val failure = IllegalStateException("Synthetic unexpected credential failure")
            coEvery {
                credentialManager.getCredential(activityContext, any<GetCredentialRequest>())
            } throws failure

            sut.signIn(activityContext).exceptionOrNull() shouldBeEqualTo failure
            coVerify(exactly = 1) {
                credentialManager.getCredential(activityContext, any<GetCredentialRequest>())
            }
        }

    private fun googleResponse() = GetCredentialResponse(googleCredential())

    private fun googleCredential() =
        GoogleIdTokenCredential(
            id = TEST_ACCOUNT_ID,
            idToken = TEST_ID_TOKEN,
            displayName = null,
            familyName = null,
            givenName = null,
            profilePictureUri = null,
            phoneNumber = null,
        )

    private suspend fun captureCancellation(block: suspend () -> Unit): CancellationException? =
        try {
            block()
            null
        } catch (exception: CancellationException) {
            exception
        }

    private fun String.sha256(): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(toByteArray())
            .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private companion object {
        const val GOOGLE_SIGN_IN_TAG = "GoogleSignInHelper"
        const val TEST_CLIENT_ID = "synthetic-client-id.apps.example.test"
        const val TEST_ACCOUNT_ID = "synthetic-account-id"
        const val TEST_ID_TOKEN = "synthetic-id-token"
    }
}
