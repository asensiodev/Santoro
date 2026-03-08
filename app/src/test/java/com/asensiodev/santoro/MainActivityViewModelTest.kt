package com.asensiodev.santoro

import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.usecase.ObserveHasSeenGuestOnboardingUseCase
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetHasSeenGuestOnboardingUseCase
import com.asensiodev.core.testing.extension.CoroutineTestExtension
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModelTest {
    @RegisterExtension
    val coroutineTestExtension = CoroutineTestExtension()

    private val observeAuthStateUseCase: ObserveAuthStateUseCase = mockk()
    private val observeHasSeenGuestOnboardingUseCase: ObserveHasSeenGuestOnboardingUseCase = mockk()
    private val observeThemeUseCase: ObserveThemeUseCase = mockk()
    private val setHasSeenGuestOnboardingUseCase: SetHasSeenGuestOnboardingUseCase = mockk()
    private val syncScheduler: WorkManagerSyncScheduler = mockk(relaxed = true)

    private lateinit var sut: MainActivityViewModel

    private val anonymousUser =
        SantoroUser(
            uid = "anon123",
            email = null,
            displayName = null,
            photoUrl = null,
            isAnonymous = true,
        )

    private val googleUser =
        SantoroUser(
            uid = "google456",
            email = "test@gmail.com",
            displayName = "Test",
            photoUrl = null,
            isAnonymous = false,
        )

    @BeforeEach
    fun setUp() {
        every { observeHasSeenGuestOnboardingUseCase() } returns flowOf(false)
        every { observeThemeUseCase() } returns flowOf(ThemeOption.SYSTEM)
    }

    private fun buildViewModel() {
        sut =
            MainActivityViewModel(
                observeAuthStateUseCase = observeAuthStateUseCase,
                observeHasSeenGuestOnboardingUseCase = observeHasSeenGuestOnboardingUseCase,
                observeThemeUseCase = observeThemeUseCase,
                setHasSeenGuestOnboardingUseCase = setHasSeenGuestOnboardingUseCase,
                syncScheduler = syncScheduler,
            )
    }

    @Test
    fun `GIVEN user becomes authenticated WHEN uiState emits Authenticated THEN schedules sync`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(anonymousUser)

            buildViewModel()
            advanceUntilIdle()

            verify(exactly = 1) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `GIVEN user is not authenticated WHEN uiState emits Unauthenticated THEN does not schedule sync`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(null)

            buildViewModel()
            advanceUntilIdle()

            verify(exactly = 0) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 0) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `GIVEN already authenticated WHEN auth state re-emits same UID THEN schedules sync only once`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(anonymousUser, anonymousUser)

            buildViewModel()
            advanceUntilIdle()

            verify(exactly = 1) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `GIVEN anonymous user WHEN UID changes to Google user THEN schedules sync again`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(anonymousUser, googleUser)

            buildViewModel()
            advanceUntilIdle()

            verify(exactly = 2) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 2) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `GIVEN repo emits DARK WHEN themeOption collected THEN StateFlow emits DARK`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(null)
            every { observeThemeUseCase() } returns flowOf(ThemeOption.DARK)

            buildViewModel()

            val values = mutableListOf<ThemeOption>()
            backgroundScope.launch {
                sut.themeOption.collect { values.add(it) }
            }
            advanceUntilIdle()

            values.last() shouldBeEqualTo ThemeOption.DARK
        }
}
