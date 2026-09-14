package com.asensiodev.auth.domain.usecase

import com.asensiodev.auth.domain.model.ExpectedUserSignOutOutcome
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.SantoroUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ExpectedUserAuthUseCaseTest {
    private val authRepository: AuthRepository = mockk()

    @Test
    fun `GIVEN expected UID and token WHEN linking Google THEN repository receives both`() =
        runTest {
            val user = SantoroUser("uid", null, null, null, false)
            coEvery { authRepository.linkWithGoogle("uid", "token") } returns Result.success(user)

            LinkWithGoogleUseCase(authRepository)("uid", "token") shouldBeEqualTo Result.success(user)

            coVerify(exactly = 1) { authRepository.linkWithGoogle("uid", "token") }
        }

    @Test
    fun `GIVEN expected UID WHEN signing out THEN repository outcome is returned`() =
        runTest {
            coEvery {
                authRepository.signOut("uid")
            } returns ExpectedUserSignOutOutcome.NoAuthenticatedUser

            SignOutUseCase(authRepository)("uid") shouldBeEqualTo
                ExpectedUserSignOutOutcome.NoAuthenticatedUser

            coVerify(exactly = 1) { authRepository.signOut("uid") }
        }
}
