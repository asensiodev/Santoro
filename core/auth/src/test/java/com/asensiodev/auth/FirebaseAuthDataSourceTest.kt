package com.asensiodev.auth

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class FirebaseAuthDataSourceTest {
    private val firebaseAuth: FirebaseAuth = mockk()
    private val sut = FirebaseAuthDataSource(firebaseAuth)

    @Test
    fun `GIVEN Firebase task cancellation WHEN signing in anonymously THEN cancellation propagates`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            every { firebaseAuth.signInAnonymously() } returns Tasks.forException<AuthResult>(cancellation)

            val thrown =
                try {
                    sut.signInAnonymously()
                    null
                } catch (exception: CancellationException) {
                    exception
                }

            thrown shouldBeEqualTo cancellation
        }

    @Test
    fun `GIVEN Firebase task cancellation WHEN deleting account THEN cancellation propagates`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            val user: FirebaseUser = mockk()
            every { firebaseAuth.currentUser } returns user
            every { user.delete() } returns Tasks.forException(cancellation)

            val thrown =
                try {
                    sut.deleteAccount()
                    null
                } catch (exception: CancellationException) {
                    exception
                }

            thrown shouldBeEqualTo cancellation
        }
}
