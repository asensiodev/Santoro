package com.asensiodev.settings.impl.domain.usecase

import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteAccountUseCaseTest {
    private val authRepository: AuthRepository = mockk()
    private val databaseRepository: DatabaseRepository = mockk(relaxed = true)

    private lateinit var sut: DeleteAccountUseCase

    @BeforeEach
    fun setUp() {
        sut = DeleteAccountUseCase(authRepository, databaseRepository)
    }

    @Test
    fun `GIVEN success WHEN invoke THEN clears database and deletes account`() =
        runTest {
            // GIVEN
            coEvery { authRepository.deleteAccount() } returns Result.success(Unit)

            // WHEN
            val result = sut()

            // THEN
            result.isSuccess shouldBeEqualTo true
        }

    @Test
    fun `GIVEN success WHEN invoke THEN clears database before deleting account`() =
        runTest {
            // GIVEN
            coEvery { authRepository.deleteAccount() } returns Result.success(Unit)

            // WHEN
            sut()

            // THEN
            coVerifyOrder {
                databaseRepository.clearAllUserData()
                authRepository.deleteAccount()
            }
        }

    @Test
    fun `GIVEN auth failure WHEN invoke THEN returns failure`() =
        runTest {
            // GIVEN
            val exception = Exception("auth error")
            coEvery { authRepository.deleteAccount() } returns Result.failure(exception)

            // WHEN
            val result = sut()

            // THEN
            result.isFailure shouldBeEqualTo true
            result.exceptionOrNull() shouldBeEqualTo exception
        }

    @Test
    fun `GIVEN auth failure WHEN invoke THEN still clears database`() =
        runTest {
            // GIVEN
            coEvery { authRepository.deleteAccount() } returns Result.failure(Exception("error"))

            // WHEN
            sut()

            // THEN
            coVerify(exactly = 1) { databaseRepository.clearAllUserData() }
        }
}
