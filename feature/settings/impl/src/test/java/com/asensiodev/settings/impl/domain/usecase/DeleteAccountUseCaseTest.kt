package com.asensiodev.settings.impl.domain.usecase

import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteAccountUseCaseTest {
    private val authRepository: AuthRepository = mockk()
    private val syncRepository: SyncRepository = mockk()
    private val recoveryRepository: AccountDeletionRecoveryRepository = mockk()

    private lateinit var sut: DeleteAccountUseCase

    @BeforeEach
    fun setUp() {
        sut = DeleteAccountUseCase(authRepository, syncRepository, recoveryRepository)
        coEvery { authRepository.reauthenticateWithGoogle(UID, ID_TOKEN) } returns Result.success(Unit)
        coEvery { syncRepository.deleteUserData(UID) } returns Result.success(Unit)
        coEvery { recoveryRepository.markLocalCleanupPending() } returns Result.success(Unit)
        coEvery { recoveryRepository.clearLocalCleanupPending() } returns Result.success(Unit)
        coEvery { authRepository.deleteAccount(UID) } returns Result.success(Unit)
    }

    @Test
    fun `GIVEN all operations succeed WHEN invoke THEN marks cleanup before deleting Auth`() =
        runTest {
            val result = sut(UID, ID_TOKEN)

            result.isSuccess shouldBeEqualTo true
            coVerifyOrder {
                authRepository.reauthenticateWithGoogle(UID, ID_TOKEN)
                syncRepository.deleteUserData(UID)
                recoveryRepository.markLocalCleanupPending()
                authRepository.deleteAccount(UID)
            }
            coVerify(exactly = 0) { recoveryRepository.clearLocalCleanupPending() }
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
            coVerify(exactly = 0) { recoveryRepository.markLocalCleanupPending() }
            coVerify(exactly = 0) { authRepository.deleteAccount(any()) }
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
        }

    @Test
    fun `GIVEN marker write is cancelled WHEN invoke THEN preserves cancellation and does not delete Auth`() =
        runTest {
            val exception = CancellationException("cancelled")
            coEvery { recoveryRepository.markLocalCleanupPending() } returns Result.failure(exception)

            val thrown = assertThrows<CancellationException> { sut(UID, ID_TOKEN) }

            thrown shouldBeEqualTo exception
            coVerify(exactly = 0) { authRepository.deleteAccount(any()) }
        }

    private companion object {
        const val UID = "uid123"
        const val ID_TOKEN = "id-token"
    }
}
