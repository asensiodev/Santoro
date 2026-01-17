package com.asensiodev.core.domain.usecase

import com.asensiodev.core.domain.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ObserveHasSeenGuestOnboardingUseCaseTest {
    private val userPreferencesRepository: UserPreferencesRepository = mockk()

    private lateinit var useCase: ObserveHasSeenGuestOnboardingUseCase

    @BeforeEach
    fun setUp() {
        useCase = ObserveHasSeenGuestOnboardingUseCase(userPreferencesRepository)
    }

    @Test
    fun `GIVEN repository returns flow WHEN invoke THEN returns expected flow`() =
        runTest {
            val expected = true
            every { userPreferencesRepository.hasSeenGuestOnboarding } returns flowOf(expected)

            val result = useCase()

            result.collect {
                it shouldBeEqualTo expected
            }
            verify { userPreferencesRepository.hasSeenGuestOnboarding }
        }
}
