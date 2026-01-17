package com.asensiodev.core.domain.usecase

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SetHasSeenGuestOnboardingUseCaseTest {
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val dispatcherProvider: DispatcherProvider = mockk()

    private lateinit var useCase: SetHasSeenGuestOnboardingUseCase

    @BeforeEach
    fun setUp() {
        val testDispatcher = UnconfinedTestDispatcher()
        every { dispatcherProvider.io } returns testDispatcher
        useCase = SetHasSeenGuestOnboardingUseCase(userPreferencesRepository, dispatcherProvider)
    }

    @Test
    fun `GIVEN hasSeen value WHEN invoke THEN calls setHasSeenGuestOnboarding in repository`() =
        runTest {
            val hasSeen = true

            useCase(hasSeen)

            coVerify { userPreferencesRepository.setHasSeenGuestOnboarding(hasSeen) }
        }
}
