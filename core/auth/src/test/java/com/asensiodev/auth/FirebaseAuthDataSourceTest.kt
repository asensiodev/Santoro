package com.asensiodev.auth

import app.cash.turbine.test
import com.asensiodev.auth.domain.exception.AccountCollisionException
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.testing.verifyOnce
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

class FirebaseAuthDataSourceTest {
    private val firebaseAuth: FirebaseAuth = mockk()
    private val sut = FirebaseAuthDataSource(firebaseAuth)

    @Test
    fun `GIVEN signed-out and signed-in states WHEN observing auth THEN values emit and listener is removed`() =
        runTest {
            val listener = slot<FirebaseAuth.AuthStateListener>()
            val firebaseUser = firebaseUser()
            var currentUser: FirebaseUser? = null
            every { firebaseAuth.currentUser } answers { currentUser }
            every { firebaseAuth.addAuthStateListener(capture(listener)) } answers {
                listener.captured.onAuthStateChanged(firebaseAuth)
            }
            every { firebaseAuth.removeAuthStateListener(any()) } returns Unit

            sut.currentUser.test {
                awaitItem().shouldBeNull()

                currentUser = firebaseUser
                listener.captured.onAuthStateChanged(firebaseAuth)

                awaitItem() shouldBeEqualTo expectedUser()
                cancelAndIgnoreRemainingEvents()
            }

            verifyOnce { firebaseAuth.addAuthStateListener(listener.captured) }
            verifyOnce { firebaseAuth.removeAuthStateListener(listener.captured) }
        }

    @Test
    fun `GIVEN successful anonymous auth result WHEN signing in anonymously THEN mapped user succeeds`() =
        runTest {
            every { firebaseAuth.signInAnonymously() } returns successfulAuthTask()

            sut.signInAnonymously() shouldBeEqualTo Result.success(expectedUser())
        }

    @Test
    fun `GIVEN anonymous auth result without user WHEN signing in anonymously THEN malformed result fails`() =
        runTest {
            every { firebaseAuth.signInAnonymously() } returns successfulAuthTask(user = null)

            sut.signInAnonymously().isFailure shouldBeEqualTo true
        }

    @Test
    fun `GIVEN failed anonymous auth task WHEN signing in anonymously THEN failure is returned`() =
        runTest {
            val failure = IllegalStateException("Synthetic anonymous auth failure")
            every { firebaseAuth.signInAnonymously() } returns Tasks.forException(failure)

            sut.signInAnonymously().exceptionOrNull() shouldBeEqualTo failure
        }

    @Test
    fun `GIVEN Firebase task cancellation WHEN signing in anonymously THEN cancellation propagates`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            every { firebaseAuth.signInAnonymously() } returns Tasks.forException<AuthResult>(cancellation)

            captureCancellation { sut.signInAnonymously() } shouldBeEqualTo cancellation
        }

    @Test
    fun `GIVEN successful Google auth result WHEN signing in with Google THEN mapped user succeeds`() =
        runTest {
            every { firebaseAuth.signInWithCredential(any()) } returns successfulAuthTask()

            sut.signInWithGoogle(TEST_ID_TOKEN) shouldBeEqualTo Result.success(expectedUser())
        }

    @Test
    fun `GIVEN Google auth result without user WHEN signing in with Google THEN malformed result fails`() =
        runTest {
            every { firebaseAuth.signInWithCredential(any()) } returns successfulAuthTask(user = null)

            sut.signInWithGoogle(TEST_ID_TOKEN).isFailure shouldBeEqualTo true
        }

    @Test
    fun `GIVEN failed Google auth task WHEN signing in with Google THEN failure is returned`() =
        runTest {
            val failure = IllegalStateException("Synthetic Google auth failure")
            every { firebaseAuth.signInWithCredential(any()) } returns Tasks.forException(failure)

            sut.signInWithGoogle(TEST_ID_TOKEN).exceptionOrNull() shouldBeEqualTo failure
        }

    @Test
    fun `GIVEN cancelled Google auth task WHEN signing in with Google THEN cancellation propagates`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            every { firebaseAuth.signInWithCredential(any()) } returns Tasks.forException(cancellation)

            captureCancellation { sut.signInWithGoogle(TEST_ID_TOKEN) } shouldBeEqualTo cancellation
        }

    @Test
    fun `GIVEN current user and successful link WHEN linking Google THEN mapped user succeeds`() =
        runTest {
            val currentUser = firebaseUser()
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.linkWithCredential(any()) } returns successfulAuthTask()

            sut.linkWithGoogle(TEST_ID_TOKEN) shouldBeEqualTo Result.success(expectedUser())
        }

    @Test
    fun `GIVEN account collision WHEN linking Google THEN domain collision failure preserves cause`() =
        runTest {
            val currentUser = firebaseUser()
            val collision: FirebaseAuthUserCollisionException = mockk()
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.linkWithCredential(any()) } returns Tasks.forException(collision)

            val failure = sut.linkWithGoogle(TEST_ID_TOKEN).exceptionOrNull()

            failure.shouldBeInstanceOf<AccountCollisionException>()
            failure?.cause shouldBeEqualTo collision
        }

    @Test
    fun `GIVEN no current user WHEN linking Google THEN missing-user failure is returned`() =
        runTest {
            every { firebaseAuth.currentUser } returns null

            val failure = sut.linkWithGoogle(TEST_ID_TOKEN).exceptionOrNull()

            failure.shouldBeInstanceOf<IllegalStateException>()
        }

    @Test
    fun `GIVEN failed link task WHEN linking Google THEN failure is returned`() =
        runTest {
            val currentUser = firebaseUser()
            val failure = IllegalStateException("Synthetic link failure")
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.linkWithCredential(any()) } returns Tasks.forException(failure)

            sut.linkWithGoogle(TEST_ID_TOKEN).exceptionOrNull() shouldBeEqualTo failure
        }

    @Test
    fun `GIVEN cancelled link task WHEN linking Google THEN cancellation propagates`() =
        runTest {
            val currentUser = firebaseUser()
            val cancellation = CancellationException("cancelled")
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.linkWithCredential(any()) } returns Tasks.forException(cancellation)

            captureCancellation { sut.linkWithGoogle(TEST_ID_TOKEN) } shouldBeEqualTo cancellation
        }

    @Test
    fun `GIVEN Firebase sign-out succeeds WHEN signing out THEN Firebase is invoked once`() =
        runTest {
            every { firebaseAuth.signOut() } returns Unit

            sut.signOut()

            verifyOnce { firebaseAuth.signOut() }
        }

    @Test
    fun `GIVEN Firebase sign-out failure WHEN signing out THEN failure propagates`() =
        runTest {
            val failure = IllegalStateException("Synthetic sign-out failure")
            every { firebaseAuth.signOut() } throws failure

            captureFailure { sut.signOut() } shouldBeEqualTo failure
        }

    @Test
    fun `GIVEN Firebase sign-out cancellation WHEN signing out THEN cancellation propagates`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            every { firebaseAuth.signOut() } throws cancellation

            captureCancellation { sut.signOut() } shouldBeEqualTo cancellation
        }

    @Test
    fun `GIVEN current user deletion succeeds WHEN deleting account THEN success is returned`() =
        runTest {
            val user = firebaseUser()
            every { firebaseAuth.currentUser } returns user
            every { user.delete() } returns Tasks.forResult(null)

            sut.deleteAccount() shouldBeEqualTo Result.success(Unit)
            verifyOnce { user.delete() }
        }

    @Test
    fun `GIVEN no current user WHEN deleting account THEN missing-user failure is returned`() =
        runTest {
            every { firebaseAuth.currentUser } returns null

            val failure = sut.deleteAccount().exceptionOrNull()

            failure.shouldBeInstanceOf<IllegalStateException>()
        }

    @Test
    fun `GIVEN user deletion failure WHEN deleting account THEN failure is returned`() =
        runTest {
            val user = firebaseUser()
            val failure = IllegalStateException("Synthetic deletion failure")
            every { firebaseAuth.currentUser } returns user
            every { user.delete() } returns Tasks.forException(failure)

            sut.deleteAccount().exceptionOrNull() shouldBeEqualTo failure
        }

    @Test
    fun `GIVEN Firebase task cancellation WHEN deleting account THEN cancellation propagates`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            val user = firebaseUser()
            every { firebaseAuth.currentUser } returns user
            every { user.delete() } returns Tasks.forException(cancellation)

            captureCancellation { sut.deleteAccount() } shouldBeEqualTo cancellation
        }

    private fun successfulAuthTask(user: FirebaseUser? = firebaseUser()) =
        Tasks.forResult<AuthResult>(
            mockk {
                every { this@mockk.user } returns user
            },
        )

    private fun firebaseUser(): FirebaseUser =
        mockk {
            every { uid } returns USER_ID
            every { email } returns USER_EMAIL
            every { displayName } returns USER_DISPLAY_NAME
            every { photoUrl } returns null
            every { isAnonymous } returns true
        }

    private fun expectedUser() =
        SantoroUser(
            uid = USER_ID,
            email = USER_EMAIL,
            displayName = USER_DISPLAY_NAME,
            photoUrl = null,
            isAnonymous = true,
        )

    private suspend fun captureCancellation(block: suspend () -> Unit): CancellationException? =
        try {
            block()
            null
        } catch (exception: CancellationException) {
            exception
        }

    private suspend fun captureFailure(block: suspend () -> Unit): Exception? =
        try {
            block()
            null
        } catch (exception: Exception) {
            exception
        }

    private companion object {
        const val TEST_ID_TOKEN = "synthetic-id-token"
        const val USER_ID = "synthetic-user-id"
        const val USER_EMAIL = "user@example.test"
        const val USER_DISPLAY_NAME = "Test User"
    }
}
