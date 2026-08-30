package com.asensiodev.settings.impl.domain.usecase

import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
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
    private val databaseRepository: DatabaseRepository = mockk()

    private lateinit var sut: DeleteAccountUseCase

    @BeforeEach
    fun setUp() {
        sut = DeleteAccountUseCase(authRepository, syncRepository, databaseRepository)
        coEvery { authRepository.reauthenticateWithGoogle(UID, ID_TOKEN) } returns Result.success(Unit)
        coEvery { syncRepository.deleteUserData(UID) } returns Result.success(Unit)
        coEvery { authRepository.deleteAccount(UID) } returns Result.success(Unit)
        coEvery { databaseRepository.clearAllUserData() } returns Result.success(Unit)
    }

    @Test
    fun `GIVEN all operations succeed WHEN invoke THEN executes every operation in exact order`() =
        runTest {
            val result = sut(UID, ID_TOKEN)

            result.isSuccess shouldBeEqualTo true
            coVerifyOrder {
                authRepository.reauthenticateWithGoogle(UID, ID_TOKEN)
                syncRepository.deleteUserData(UID)
                authRepository.deleteAccount(UID)
                databaseRepository.clearAllUserData()
            }
            coVerify(exactly = 1) { authRepository.reauthenticateWithGoogle(UID, ID_TOKEN) }
            coVerify(exactly = 1) { syncRepository.deleteUserData(UID) }
            coVerify(exactly = 1) { authRepository.deleteAccount(UID) }
            coVerify(exactly = 1) { databaseRepository.clearAllUserData() }
        }

    @Test
    fun `GIVEN reauthentication fails WHEN invoke THEN returns failure and performs no deletion`() =
        runTest {
            val exception = Exception("Reauthentication")
            coEvery {
                authRepository.reauthenticateWithGoogle(UID, ID_TOKEN)
            } returns Result.failure(exception)

            val result = sut(UID, ID_TOKEN)

            result.exceptionOrNull() shouldBeEqualTo exception
            coVerify(exactly = 0) { syncRepository.deleteUserData(any()) }
            coVerify(exactly = 0) { authRepository.deleteAccount(any()) }
            coVerify(exactly = 0) { databaseRepository.clearAllUserData() }
        }

    @Test
    fun `GIVEN Firestore deletion fails WHEN invoke THEN returns failure and stops`() =
        runTest {
            val exception = Exception("Firestore")
            coEvery { syncRepository.deleteUserData(UID) } returns Result.failure(exception)

            val result = sut(UID, ID_TOKEN)

            result.exceptionOrNull() shouldBeEqualTo exception
            coVerify(exactly = 1) { authRepository.reauthenticateWithGoogle(UID, ID_TOKEN) }
            coVerify(exactly = 0) { authRepository.deleteAccount(any()) }
            coVerify(exactly = 0) { databaseRepository.clearAllUserData() }
        }

    @Test
    fun `GIVEN Auth deletion fails WHEN invoke THEN returns failure and does not clear Room`() =
        runTest {
            val exception = Exception("Auth")
            coEvery { authRepository.deleteAccount(UID) } returns Result.failure(exception)

            val result = sut(UID, ID_TOKEN)

            result.exceptionOrNull() shouldBeEqualTo exception
            coVerify(exactly = 1) { syncRepository.deleteUserData(UID) }
            coVerify(exactly = 0) { databaseRepository.clearAllUserData() }
        }

    @Test
    fun `GIVEN Room cleanup fails WHEN invoke THEN returns Room failure`() =
        runTest {
            val exception = Exception("Room")
            coEvery { databaseRepository.clearAllUserData() } returns Result.failure(exception)

            val result = sut(UID, ID_TOKEN)

            result.exceptionOrNull() shouldBeEqualTo exception
            coVerify(exactly = 1) { databaseRepository.clearAllUserData() }
        }

    @Test
    fun `GIVEN reauthentication is cancelled WHEN invoke THEN cancellation is preserved`() =
        runTest {
            val exception = CancellationException("cancelled")
            coEvery {
                authRepository.reauthenticateWithGoogle(UID, ID_TOKEN)
            } returns Result.failure(exception)

            val thrown = assertThrows<CancellationException> { sut(UID, ID_TOKEN) }

            thrown shouldBeEqualTo exception
            coVerify(exactly = 0) { syncRepository.deleteUserData(any()) }
            coVerify(exactly = 0) { authRepository.deleteAccount(any()) }
            coVerify(exactly = 0) { databaseRepository.clearAllUserData() }
        }

    @Test
    fun `GIVEN Firestore deletion is cancelled WHEN invoke THEN cancellation is preserved`() =
        runTest {
            val exception = CancellationException("cancelled")
            coEvery { syncRepository.deleteUserData(UID) } returns Result.failure(exception)

            val thrown = assertThrows<CancellationException> { sut(UID, ID_TOKEN) }

            thrown shouldBeEqualTo exception
            coVerify(exactly = 0) { authRepository.deleteAccount(any()) }
            coVerify(exactly = 0) { databaseRepository.clearAllUserData() }
        }

    @Test
    fun `GIVEN Auth deletion is cancelled WHEN invoke THEN cancellation is preserved`() =
        runTest {
            val exception = CancellationException("cancelled")
            coEvery { authRepository.deleteAccount(UID) } returns Result.failure(exception)

            val thrown = assertThrows<CancellationException> { sut(UID, ID_TOKEN) }

            thrown shouldBeEqualTo exception
            coVerify(exactly = 0) { databaseRepository.clearAllUserData() }
        }

    @Test
    fun `GIVEN Room cleanup returns cancellation WHEN invoke THEN cancellation is preserved`() =
        runTest {
            val exception = CancellationException("cancelled")
            coEvery { databaseRepository.clearAllUserData() } returns Result.failure(exception)

            val thrown = assertThrows<CancellationException> { sut(UID, ID_TOKEN) }

            thrown shouldBeEqualTo exception
        }

    private companion object {
        const val UID = "uid123"
        const val ID_TOKEN = "id-token"
    }
}
