package com.asensiodev.auth

import app.cash.turbine.test
import com.asensiodev.auth.domain.exception.AccountCollisionException
import com.asensiodev.auth.domain.exception.AuthenticatedUserMismatchException
import com.asensiodev.auth.domain.exception.NoAuthenticatedUserException
import com.asensiodev.auth.domain.model.ExpectedUserSignOutOutcome
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.testing.verifyNever
import com.asensiodev.core.testing.verifyOnce
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verifyOrder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthDataSourceTest {
    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val sut =
        FirebaseAuthDataSource(
            firebaseAuth,
            CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        )

    @Test
    fun `GIVEN auth changes WHEN observing auth THEN values emit from one application listener`() =
        runTest {
            val auth: FirebaseAuth = mockk()
            val listener = slot<FirebaseAuth.AuthStateListener>()
            val firebaseUser = firebaseUser()
            var currentUser: FirebaseUser? = null
            every { auth.currentUser } answers { currentUser }
            every { auth.addAuthStateListener(capture(listener)) } answers {
                listener.captured.onAuthStateChanged(auth)
            }
            every { auth.removeAuthStateListener(any()) } returns Unit
            val applicationScope = CoroutineScope(coroutineContext + SupervisorJob())
            val source = FirebaseAuthDataSource(auth, applicationScope)
            runCurrent()

            source.currentUser.test {
                awaitItem().shouldBeNull()

                currentUser = firebaseUser
                listener.captured.onAuthStateChanged(auth)

                awaitItem() shouldBeEqualTo expectedUser()
                cancelAndIgnoreRemainingEvents()
            }

            verifyOnce { auth.addAuthStateListener(listener.captured) }
            verifyNever { auth.removeAuthStateListener(any()) }
            applicationScope.cancel()
            runCurrent()
            verifyOnce { auth.removeAuthStateListener(listener.captured) }
        }

    @Test
    fun `GIVEN no collectors WHEN auth changes THEN reused collection has latest replay and one listener`() =
        runTest {
            val auth: FirebaseAuth = mockk()
            val listener = slot<FirebaseAuth.AuthStateListener>()
            val firebaseUser = firebaseUser()
            var currentUser: FirebaseUser? = null
            every { auth.currentUser } answers { currentUser }
            every { auth.addAuthStateListener(capture(listener)) } answers {
                listener.captured.onAuthStateChanged(auth)
            }
            every { auth.removeAuthStateListener(any()) } returns Unit
            val applicationScope = CoroutineScope(coroutineContext + SupervisorJob())
            val source = FirebaseAuthDataSource(auth, applicationScope)
            runCurrent()

            source.currentUser.test {
                awaitItem().shouldBeNull()
                cancelAndIgnoreRemainingEvents()
            }
            currentUser = firebaseUser
            listener.captured.onAuthStateChanged(auth)
            runCurrent()
            source.currentUser.test {
                awaitItem() shouldBeEqualTo expectedUser()
                cancelAndIgnoreRemainingEvents()
            }

            verifyOnce { auth.addAuthStateListener(listener.captured) }
            verifyNever { auth.removeAuthStateListener(any()) }
            applicationScope.cancel()
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

            sut.linkWithGoogle(USER_ID, TEST_ID_TOKEN) shouldBeEqualTo Result.success(expectedUser())
        }

    @Test
    fun `GIVEN account collision WHEN linking Google THEN domain collision failure preserves cause`() =
        runTest {
            val currentUser = firebaseUser()
            val collision: FirebaseAuthUserCollisionException = mockk()
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.linkWithCredential(any()) } returns Tasks.forException(collision)

            val failure = sut.linkWithGoogle(USER_ID, TEST_ID_TOKEN).exceptionOrNull()

            failure.shouldBeInstanceOf<AccountCollisionException>()
            failure?.cause shouldBeEqualTo collision
        }

    @Test
    fun `GIVEN no current user WHEN linking Google THEN missing-user failure is returned`() =
        runTest {
            every { firebaseAuth.currentUser } returns null

            val failure = sut.linkWithGoogle(USER_ID, TEST_ID_TOKEN).exceptionOrNull()

            failure.shouldBeInstanceOf<NoAuthenticatedUserException>()
        }

    @Test
    fun `GIVEN failed link task WHEN linking Google THEN failure is returned`() =
        runTest {
            val currentUser = firebaseUser()
            val failure = IllegalStateException("Synthetic link failure")
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.linkWithCredential(any()) } returns Tasks.forException(failure)

            sut.linkWithGoogle(USER_ID, TEST_ID_TOKEN).exceptionOrNull() shouldBeEqualTo failure
        }

    @Test
    fun `GIVEN cancelled link task WHEN linking Google THEN cancellation propagates`() =
        runTest {
            val currentUser = firebaseUser()
            val cancellation = CancellationException("cancelled")
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.linkWithCredential(any()) } returns Tasks.forException(cancellation)

            captureCancellation {
                sut.linkWithGoogle(USER_ID, TEST_ID_TOKEN)
            } shouldBeEqualTo cancellation
        }

    @Test
    fun `GIVEN current user UID differs WHEN linking Google THEN mismatch fails before link starts`() =
        runTest {
            val currentUser = firebaseUser()
            every { firebaseAuth.currentUser } returns currentUser

            val failure = sut.linkWithGoogle("different-user", TEST_ID_TOKEN).exceptionOrNull()

            failure.shouldBeInstanceOf<AuthenticatedUserMismatchException>()
            verifyNever { currentUser.linkWithCredential(any()) }
        }

    @Test
    fun `GIVEN linked result UID differs WHEN linking Google THEN mismatch failure is returned`() =
        runTest {
            val currentUser = firebaseUser()
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.linkWithCredential(any()) } returns
                successfulAuthTask(firebaseUser(uid = "different-user"))

            val failure = sut.linkWithGoogle(USER_ID, TEST_ID_TOKEN).exceptionOrNull()

            failure.shouldBeInstanceOf<AuthenticatedUserMismatchException>()
        }

    @Test
    fun `GIVEN linked result user A but current Auth B after suspension WHEN linking THEN mismatch fails`() =
        runTest {
            val userA = firebaseUser()
            val userB = firebaseUser(uid = "different-user")
            val taskSource = TaskCompletionSource<AuthResult>()
            var currentUser = userA
            every { firebaseAuth.currentUser } answers { currentUser }
            every { userA.linkWithCredential(any()) } returns taskSource.task

            val result = async { sut.linkWithGoogle(USER_ID, TEST_ID_TOKEN) }
            runCurrent()
            currentUser = userB
            taskSource.setResult(authResult(userA))

            result.await().exceptionOrNull().shouldBeInstanceOf<AuthenticatedUserMismatchException>()
        }

    @Test
    fun `GIVEN accepted link task WHEN caller is cancelled THEN task may settle later`() =
        runTest {
            val currentUser = firebaseUser()
            val taskSource = TaskCompletionSource<AuthResult>()
            var cancellation: CancellationException? = null
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.linkWithCredential(any()) } returns taskSource.task

            val caller =
                launch {
                    try {
                        sut.linkWithGoogle(USER_ID, TEST_ID_TOKEN)
                    } catch (exception: CancellationException) {
                        cancellation = exception
                    }
                }
            testScheduler.runCurrent()

            caller.cancelAndJoin()
            taskSource.setResult(authResult(firebaseUser()))

            cancellation.shouldBeInstanceOf<CancellationException>()
            taskSource.task.isSuccessful shouldBeEqualTo true
            verifyOnce { currentUser.linkWithCredential(any()) }
        }

    @Test
    fun `GIVEN current user WHEN reauthenticating with Google THEN user is reauthenticated`() =
        runTest {
            val currentUser = firebaseUser()
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.reauthenticate(any()) } returns Tasks.forResult(null)

            sut.reauthenticateWithGoogle(USER_ID, TEST_ID_TOKEN) shouldBeEqualTo Result.success(Unit)

            verifyOnce { currentUser.reauthenticate(any()) }
        }

    @Test
    fun `GIVEN no current user WHEN reauthenticating with Google THEN missing-user failure is returned`() =
        runTest {
            every { firebaseAuth.currentUser } returns null

            val failure = sut.reauthenticateWithGoogle(USER_ID, TEST_ID_TOKEN).exceptionOrNull()

            failure.shouldBeInstanceOf<NoAuthenticatedUserException>()
        }

    @Test
    fun `GIVEN reauthentication failure WHEN reauthenticating with Google THEN failure is returned`() =
        runTest {
            val currentUser = firebaseUser()
            val failure = IllegalStateException("Synthetic reauthentication failure")
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.reauthenticate(any()) } returns Tasks.forException(failure)

            sut.reauthenticateWithGoogle(USER_ID, TEST_ID_TOKEN).exceptionOrNull() shouldBeEqualTo failure
        }

    @Test
    fun `GIVEN current user UID differs WHEN reauthenticating THEN failure is returned without reauthentication`() =
        runTest {
            val currentUser = firebaseUser()
            every { firebaseAuth.currentUser } returns currentUser

            val failure = sut.reauthenticateWithGoogle("different-user", TEST_ID_TOKEN).exceptionOrNull()

            failure.shouldBeInstanceOf<AuthenticatedUserMismatchException>()
            verifyNever { currentUser.reauthenticate(any()) }
        }

    @Test
    fun `GIVEN reauthentication cancellation WHEN reauthenticating with Google THEN cancellation propagates`() =
        runTest {
            val currentUser = firebaseUser()
            val cancellation = CancellationException("cancelled")
            every { firebaseAuth.currentUser } returns currentUser
            every { currentUser.reauthenticate(any()) } returns Tasks.forException(cancellation)

            captureCancellation {
                sut.reauthenticateWithGoogle(USER_ID, TEST_ID_TOKEN)
            } shouldBeEqualTo cancellation
        }

    @Test
    fun `GIVEN Firebase sign-out succeeds WHEN signing out THEN Firebase is invoked once`() =
        runTest {
            every { firebaseAuth.currentUser } returns firebaseUser()
            every { firebaseAuth.signOut() } returns Unit

            sut.signOut(USER_ID) shouldBeEqualTo ExpectedUserSignOutOutcome.SignedOut

            verifyOrder {
                firebaseAuth.currentUser
                firebaseAuth.signOut()
            }
        }

    @Test
    fun `GIVEN Firebase sign-out failure WHEN signing out THEN failure propagates`() =
        runTest {
            val failure = IllegalStateException("Synthetic sign-out failure")
            every { firebaseAuth.currentUser } returns firebaseUser()
            every { firebaseAuth.signOut() } throws failure

            captureFailure { sut.signOut(USER_ID) } shouldBeEqualTo failure
        }

    @Test
    fun `GIVEN Firebase sign-out cancellation WHEN signing out THEN cancellation propagates`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            every { firebaseAuth.currentUser } returns firebaseUser()
            every { firebaseAuth.signOut() } throws cancellation

            captureCancellation { sut.signOut(USER_ID) } shouldBeEqualTo cancellation
        }

    @Test
    fun `GIVEN no authenticated user WHEN signing out THEN no-user outcome does not invoke Firebase sign-out`() =
        runTest {
            every { firebaseAuth.currentUser } returns null

            sut.signOut(USER_ID) shouldBeEqualTo ExpectedUserSignOutOutcome.NoAuthenticatedUser

            verifyNever { firebaseAuth.signOut() }
        }

    @Test
    fun `GIVEN authenticated UID differs WHEN signing out THEN mismatch outcome does not invoke Firebase sign-out`() =
        runTest {
            every { firebaseAuth.currentUser } returns firebaseUser(uid = "different-user")

            sut.signOut(USER_ID) shouldBeEqualTo ExpectedUserSignOutOutcome.AuthenticatedUserMismatch

            verifyNever { firebaseAuth.signOut() }
        }

    @Test
    fun `GIVEN current user deletion succeeds WHEN deleting account THEN success is returned`() =
        runTest {
            val user = firebaseUser()
            every { firebaseAuth.currentUser } returns user
            every { user.delete() } returns Tasks.forResult(null)

            sut.deleteAccount(USER_ID) shouldBeEqualTo Result.success(Unit)
            verifyOnce { user.delete() }
        }

    @Test
    fun `GIVEN no current user WHEN deleting account THEN missing-user failure is returned`() =
        runTest {
            every { firebaseAuth.currentUser } returns null

            val failure = sut.deleteAccount(USER_ID).exceptionOrNull()

            failure.shouldBeInstanceOf<NoAuthenticatedUserException>()
        }

    @Test
    fun `GIVEN user deletion failure WHEN deleting account THEN failure is returned`() =
        runTest {
            val user = firebaseUser()
            val failure = IllegalStateException("Synthetic deletion failure")
            every { firebaseAuth.currentUser } returns user
            every { user.delete() } returns Tasks.forException(failure)

            sut.deleteAccount(USER_ID).exceptionOrNull() shouldBeEqualTo failure
        }

    @Test
    fun `GIVEN current user UID differs WHEN deleting account THEN failure is returned without deletion`() =
        runTest {
            val user = firebaseUser()
            every { firebaseAuth.currentUser } returns user

            val failure = sut.deleteAccount("different-user").exceptionOrNull()

            failure.shouldBeInstanceOf<AuthenticatedUserMismatchException>()
            verifyNever { user.delete() }
        }

    @Test
    fun `GIVEN Firebase task cancellation WHEN deleting account THEN cancellation propagates`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            val user = firebaseUser()
            every { firebaseAuth.currentUser } returns user
            every { user.delete() } returns Tasks.forException(cancellation)

            captureCancellation { sut.deleteAccount(USER_ID) } shouldBeEqualTo cancellation
        }

    private fun successfulAuthTask(user: FirebaseUser? = firebaseUser()) = Tasks.forResult(authResult(user))

    private fun authResult(user: FirebaseUser?): AuthResult =
        mockk {
            every { this@mockk.user } returns user
        }

    private fun firebaseUser(uid: String = USER_ID): FirebaseUser =
        mockk {
            every { this@mockk.uid } returns uid
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
