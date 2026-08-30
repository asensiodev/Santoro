package com.asensiodev.auth.data.repository

import com.asensiodev.auth.AuthDataSource
import com.asensiodev.library.observability.api.ObservabilityTracker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultAuthRepositoryTest {
    private val dataSource: AuthDataSource = mockk()
    private val observabilityTracker: ObservabilityTracker = mockk(relaxed = true)
    private lateinit var sut: DefaultAuthRepository

    @BeforeEach
    fun setUp() {
        every { dataSource.currentUser } returns emptyFlow()
        sut = DefaultAuthRepository(dataSource, observabilityTracker)
    }

    @Test
    fun `GIVEN wrapped cancellation WHEN signing in anonymously THEN cancellation propagates without logging`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            coEvery { dataSource.signInAnonymously() } returns Result.failure(cancellation)

            val thrown = captureCancellation { sut.signInAnonymously() }

            thrown shouldBeEqualTo cancellation
            verify(exactly = 0) { observabilityTracker.recordError(any(), any(), any()) }
        }

    @Test
    fun `GIVEN wrapped cancellation WHEN signing in with Google THEN cancellation propagates without logging`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            coEvery { dataSource.signInWithGoogle("token") } returns Result.failure(cancellation)

            val thrown = captureCancellation { sut.signInWithGoogle("token") }

            thrown shouldBeEqualTo cancellation
            verify(exactly = 0) { observabilityTracker.recordError(any(), any(), any()) }
        }

    @Test
    fun `GIVEN wrapped cancellation WHEN linking Google THEN cancellation propagates without logging`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            coEvery { dataSource.linkWithGoogle("token") } returns Result.failure(cancellation)

            val thrown = captureCancellation { sut.linkWithGoogle("token") }

            thrown shouldBeEqualTo cancellation
            verify(exactly = 0) { observabilityTracker.recordError(any(), any(), any()) }
        }

    @Test
    fun `GIVEN successful Google reauthentication WHEN reauthenticating THEN success is tracked`() =
        runTest {
            coEvery { dataSource.reauthenticateWithGoogle("uid", "token") } returns Result.success(Unit)

            sut.reauthenticateWithGoogle("uid", "token") shouldBeEqualTo Result.success(Unit)

            coVerify(exactly = 1) { dataSource.reauthenticateWithGoogle("uid", "token") }
            verify(exactly = 1) { observabilityTracker.trackAction("auth_reauthenticate_google") }
        }

    @Test
    fun `GIVEN failed Google reauthentication WHEN reauthenticating THEN failure is tracked`() =
        runTest {
            val failure = IllegalStateException("reauthentication failed")
            coEvery {
                dataSource.reauthenticateWithGoogle("uid", "token")
            } returns Result.failure(failure)

            sut.reauthenticateWithGoogle("uid", "token").exceptionOrNull() shouldBeEqualTo failure

            verify(exactly = 1) {
                observabilityTracker.recordError("auth_reauthenticate_google_failed", failure)
            }
        }

    @Test
    fun `GIVEN wrapped cancellation WHEN reauthenticating THEN cancellation propagates without logging`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            coEvery {
                dataSource.reauthenticateWithGoogle("uid", "token")
            } returns Result.failure(cancellation)

            val thrown = captureCancellation { sut.reauthenticateWithGoogle("uid", "token") }

            thrown shouldBeEqualTo cancellation
            verify(exactly = 0) { observabilityTracker.recordError(any(), any(), any()) }
        }

    @Test
    fun `GIVEN wrapped cancellation WHEN deleting account THEN cancellation propagates without logging`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            coEvery { dataSource.deleteAccount("uid") } returns Result.failure(cancellation)

            val thrown = captureCancellation { sut.deleteAccount("uid") }

            thrown shouldBeEqualTo cancellation
            verify(exactly = 0) { observabilityTracker.recordError(any(), any(), any()) }
        }

    private suspend fun captureCancellation(operation: suspend () -> Result<*>): CancellationException? =
        try {
            operation()
            null
        } catch (exception: CancellationException) {
            exception
        }
}
