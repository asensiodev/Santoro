package com.asensiodev.settings.impl.domain.usecase

import com.asensiodev.auth.domain.exception.AuthenticatedUserMismatchException
import com.asensiodev.auth.domain.exception.NoAuthenticatedUserException
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.repository.SyncRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteAccountUseCaseTest {
    private val authRepository: AuthRepository = mockk()
    private val syncRepository: SyncRepository = mockk()
    private val recoveryRepository: AccountDeletionRecoveryRepository = mockk()

    private lateinit var sut: DeleteAccountUseCase

    @BeforeEach
    fun setUp() {
        sut = DeleteAccountUseCase(authRepository, syncRepository, recoveryRepository)
        coEvery { authRepository.reauthenticateWithGoogle(UID, ID_TOKEN) } returns Result.success(Unit)
        every { authRepository.currentUser } returns flowOf(user())
        coEvery { syncRepository.deleteUserData(UID) } returns Result.success(Unit)
        coEvery { recoveryRepository.markLocalCleanupPending() } returns Result.success(Unit)
        coEvery { recoveryRepository.clearLocalCleanupPending() } returns Result.success(Unit)
        coEvery { authRepository.deleteAccount(UID) } returns Result.success(Unit)
        every { recoveryRepository.beginRemoteDeletion() } returns Unit
        every { recoveryRepository.completeRemoteDeletion() } returns Unit
    }

    @Test
    fun `GIVEN all operations succeed WHEN invoke THEN marks cleanup before deleting Auth`() =
        runTest {
            val result = sut(UID, ID_TOKEN)

            result.isSuccess shouldBeEqualTo true
            coVerifyOrder {
                authRepository.reauthenticateWithGoogle(UID, ID_TOKEN)
                recoveryRepository.beginRemoteDeletion()
                authRepository.currentUser
                syncRepository.deleteUserData(UID)
                recoveryRepository.markLocalCleanupPending()
                authRepository.deleteAccount(UID)
            }
            coVerify(exactly = 0) { recoveryRepository.clearLocalCleanupPending() }
            verify(exactly = 1) { recoveryRepository.completeRemoteDeletion() }
        }

    @Test
    fun `GIVEN current user is missing after reauthentication WHEN invoke THEN stops within lease`() =
        runTest {
            every { authRepository.currentUser } returns flowOf(null)

            val result = sut(UID, ID_TOKEN)

            result.exceptionOrNull().shouldBeInstanceOf<NoAuthenticatedUserException>()
            verifyGuardFailureSideEffects()
        }

    @Test
    fun `GIVEN current user differs after reauthentication WHEN invoke THEN stops within lease`() =
        runTest {
            every { authRepository.currentUser } returns flowOf(user(uid = "different-user"))

            val result = sut(UID, ID_TOKEN)

            result.exceptionOrNull().shouldBeInstanceOf<AuthenticatedUserMismatchException>()
            verifyGuardFailureSideEffects()
        }

    @Test
    fun `GIVEN Auth changes while reauthentication is suspended WHEN it succeeds THEN guard rejects new user`() =
        runTest {
            val reauthentication = CompletableDeferred<Result<Unit>>()
            val currentUser = MutableStateFlow<SantoroUser?>(user())
            coEvery {
                authRepository.reauthenticateWithGoogle(UID, ID_TOKEN)
            } coAnswers { reauthentication.await() }
            every { authRepository.currentUser } returns currentUser

            val result = async { sut(UID, ID_TOKEN) }
            runCurrent()
            currentUser.value = user(uid = "different-user")
            reauthentication.complete(Result.success(Unit))

            result.await().exceptionOrNull().shouldBeInstanceOf<AuthenticatedUserMismatchException>()
            verifyGuardFailureSideEffects()
        }

    @Test
    fun `GIVEN current user read is cancelled WHEN invoke THEN releases lease and propagates cancellation`() =
        runTest {
            val exception = CancellationException("cancelled")
            every { authRepository.currentUser } returns flow { throw exception }

            val thrown = assertThrows<CancellationException> { sut(UID, ID_TOKEN) }

            thrown shouldBeEqualTo exception
            verifyGuardFailureSideEffects()
        }

    @Test
    fun `GIVEN reauthentication fails WHEN invoke THEN returns failure and stops`() =
        runTest {
            val exception = Exception("Reauthentication")
            coEvery {
                authRepository.reauthenticateWithGoogle(UID, ID_TOKEN)
            } returns Result.failure(exception)

            val result = sut(UID, ID_TOKEN)

            result.exceptionOrNull() shouldBeEqualTo exception
            coVerify(exactly = 0) { syncRepository.deleteUserData(any()) }
            verify(exactly = 0) { recoveryRepository.beginRemoteDeletion() }
            coVerify(exactly = 0) { recoveryRepository.markLocalCleanupPending() }
            coVerify(exactly = 0) { authRepository.deleteAccount(any()) }
            verify(exactly = 0) { recoveryRepository.completeRemoteDeletion() }
        }

    @Test
    fun `GIVEN Firestore deletion fails WHEN invoke THEN does not mark cleanup or delete Auth`() =
        runTest {
            val exception = Exception("Firestore")
            coEvery { syncRepository.deleteUserData(UID) } returns Result.failure(exception)

            val result = sut(UID, ID_TOKEN)

            result.exceptionOrNull() shouldBeEqualTo exception
            coVerify(exactly = 0) { recoveryRepository.markLocalCleanupPending() }
            coVerify(exactly = 0) { authRepository.deleteAccount(any()) }
        }

    @Test
    fun `GIVEN cleanup marker fails WHEN invoke THEN does not delete Auth`() =
        runTest {
            val exception = Exception("Marker")
            coEvery { recoveryRepository.markLocalCleanupPending() } returns Result.failure(exception)

            val result = sut(UID, ID_TOKEN)

            result.exceptionOrNull() shouldBeEqualTo exception
            coVerify(exactly = 0) { authRepository.deleteAccount(any()) }
        }

    @Test
    fun `GIVEN Auth deletion fails WHEN invoke THEN clears marker and returns Auth failure`() =
        runTest {
            val exception = Exception("Auth")
            coEvery { authRepository.deleteAccount(UID) } returns Result.failure(exception)

            val result = sut(UID, ID_TOKEN)

            result.exceptionOrNull() shouldBeEqualTo exception
            coVerify(exactly = 1) { recoveryRepository.clearLocalCleanupPending() }
        }

    @Test
    fun `GIVEN Auth and marker clear fail WHEN invoke THEN returns marker failure`() =
        runTest {
            val markerException = Exception("Marker clear")
            coEvery { authRepository.deleteAccount(UID) } returns Result.failure(Exception("Auth"))
            coEvery {
                recoveryRepository.clearLocalCleanupPending()
            } returns Result.failure(markerException)

            val result = sut(UID, ID_TOKEN)

            result.exceptionOrNull() shouldBeEqualTo markerException
        }

    @Test
    fun `GIVEN Auth deletion is cancelled WHEN invoke THEN preserves marker and cancellation`() =
        runTest {
            val exception = CancellationException("cancelled")
            coEvery { authRepository.deleteAccount(UID) } returns Result.failure(exception)

            val thrown = assertThrows<CancellationException> { sut(UID, ID_TOKEN) }

            thrown shouldBeEqualTo exception
            coVerify(exactly = 0) { recoveryRepository.clearLocalCleanupPending() }
            verify(exactly = 1) { recoveryRepository.completeRemoteDeletion() }
        }

    @Test
    fun `GIVEN marker write is cancelled WHEN invoke THEN preserves cancellation and does not delete Auth`() =
        runTest {
            val exception = CancellationException("cancelled")
            coEvery { recoveryRepository.markLocalCleanupPending() } returns Result.failure(exception)

            val thrown = assertThrows<CancellationException> { sut(UID, ID_TOKEN) }

            thrown shouldBeEqualTo exception
            coVerify(exactly = 0) { authRepository.deleteAccount(any()) }
            verify(exactly = 1) { recoveryRepository.completeRemoteDeletion() }
        }

    @Test
    fun `GIVEN Auth deletion is in flight WHEN marker is active THEN marker remains authoritative`() =
        runTest {
            val deletion = CompletableDeferred<Result<Unit>>()
            coEvery { authRepository.deleteAccount(UID) } coAnswers { deletion.await() }

            val job = launch { sut(UID, ID_TOKEN) }
            runCurrent()

            coVerifyOrder {
                recoveryRepository.markLocalCleanupPending()
                authRepository.deleteAccount(UID)
            }
            coVerify(exactly = 0) { recoveryRepository.clearLocalCleanupPending() }

            deletion.complete(Result.success(Unit))
            job.join()
            coVerify(exactly = 0) { recoveryRepository.clearLocalCleanupPending() }
            verify(exactly = 1) { recoveryRepository.completeRemoteDeletion() }
        }

    @Test
    fun `GIVEN Firestore deletion is in flight WHEN invoked THEN remote lease remains active`() =
        runTest {
            val deletion = CompletableDeferred<Result<Unit>>()
            coEvery { syncRepository.deleteUserData(UID) } coAnswers { deletion.await() }

            val job = launch { sut(UID, ID_TOKEN) }
            runCurrent()

            verify(exactly = 1) { recoveryRepository.beginRemoteDeletion() }
            verify(exactly = 0) { recoveryRepository.completeRemoteDeletion() }
            coVerify(exactly = 0) { recoveryRepository.markLocalCleanupPending() }

            deletion.complete(Result.failure(Exception("Firestore")))
            job.join()
            verify(exactly = 1) { recoveryRepository.completeRemoteDeletion() }
        }

    @Test
    fun `GIVEN Firestore deletion throws unexpectedly WHEN invoked THEN releases remote lease`() =
        runTest {
            val exception = IllegalStateException("unexpected")
            coEvery { syncRepository.deleteUserData(UID) } throws exception

            val thrown = assertThrows<IllegalStateException> { sut(UID, ID_TOKEN) }

            thrown shouldBeEqualTo exception
            verify(exactly = 1) { recoveryRepository.completeRemoteDeletion() }
            coVerify(exactly = 0) { recoveryRepository.markLocalCleanupPending() }
        }

    @Test
    fun `GIVEN Firestore deletion is cancelled WHEN invoked THEN releases lease and preserves cancellation`() =
        runTest {
            val exception = CancellationException("cancelled")
            coEvery { syncRepository.deleteUserData(UID) } returns Result.failure(exception)

            val thrown = assertThrows<CancellationException> { sut(UID, ID_TOKEN) }

            thrown shouldBeEqualTo exception
            verify(exactly = 1) { recoveryRepository.completeRemoteDeletion() }
            coVerify(exactly = 0) { recoveryRepository.markLocalCleanupPending() }
        }

    private fun verifyGuardFailureSideEffects() {
        verify(exactly = 1) { recoveryRepository.beginRemoteDeletion() }
        verify(exactly = 1) { recoveryRepository.completeRemoteDeletion() }
        coVerify(exactly = 0) { syncRepository.deleteUserData(any()) }
        coVerify(exactly = 0) { recoveryRepository.markLocalCleanupPending() }
        coVerify(exactly = 0) { recoveryRepository.clearLocalCleanupPending() }
        coVerify(exactly = 0) { authRepository.deleteAccount(any()) }
    }

    private fun user(uid: String = UID) = SantoroUser(uid, null, null, null, false)

    private companion object {
        const val UID = "uid123"
        const val ID_TOKEN = "id-token"
    }
}
