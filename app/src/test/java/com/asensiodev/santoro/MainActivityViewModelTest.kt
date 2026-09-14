package com.asensiodev.santoro

import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.repository.MovieMutationRepository
import com.asensiodev.core.domain.repository.SyncScheduler
import com.asensiodev.core.domain.usecase.ObserveHasSeenGuestOnboardingUseCase
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetHasSeenGuestOnboardingUseCase
import com.asensiodev.core.testing.extension.CoroutineTestExtension
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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
    private val setHasSeenGuestOnboardingUseCase: SetHasSeenGuestOnboardingUseCase = mockk(relaxed = true)
    private val syncScheduler: SyncScheduler = mockk(relaxed = true)
    private val movieMutationRepository: MovieMutationRepository = mockk()
    private val deletionRecoveryRepository: AccountDeletionRecoveryRepository = mockk()
    private val authState = MutableSharedFlow<SantoroUser?>(replay = 1, extraBufferCapacity = 16)
    private val deletionPending = MutableStateFlow(false)
    private val remoteDeletionInFlight = MutableStateFlow(false)
    private lateinit var sut: MainActivityViewModel

    @BeforeEach
    fun setUp() {
        authState.tryEmit(null)
        every { observeAuthStateUseCase() } returns authState
        every { observeHasSeenGuestOnboardingUseCase() } returns flowOf(false)
        every { observeThemeUseCase() } returns flowOf(ThemeOption.SYSTEM)
        every { deletionRecoveryRepository.isLocalCleanupPending } returns deletionPending
        every { deletionRecoveryRepository.isRemoteDeletionInFlight } returns remoteDeletionInFlight
        coJustRun { movieMutationRepository.clearMovies() }
        coEvery { deletionRecoveryRepository.clearLocalCleanupPending() } coAnswers {
            deletionPending.value = false
            Result.success(Unit)
        }
    }

    @Test
    fun `initial authenticated session retains movies and schedules sync`() =
        runTest {
            authState.tryEmit(USER_A)

            buildViewModel()
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo authenticated(USER_A)
            coVerify(exactly = 0) { movieMutationRepository.clearMovies() }
            verify(exactly = 1) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `auth null clears movies before publishing login`() =
        runTest {
            val clearMovies = CompletableDeferred<Unit>()
            coEvery { movieMutationRepository.clearMovies() } coAnswers { clearMovies.await() }

            buildViewModel()
            runCurrent()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.Loading
            clearMovies.complete(Unit)
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.Unauthenticated
            coVerify(exactly = 1) { movieMutationRepository.clearMovies() }
        }

    @Test
    fun `login after observed null clears movies a second time`() =
        runTest {
            buildViewModel()
            advanceUntilIdle()
            val secondClear = CompletableDeferred<Unit>()
            coEvery { movieMutationRepository.clearMovies() } coAnswers { secondClear.await() }

            authState.emit(USER_B)
            runCurrent()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.Loading
            verify(exactly = 0) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 0) { syncScheduler.scheduleImmediateSync() }

            secondClear.complete(Unit)
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo authenticated(USER_B)
            coVerify(exactly = 2) { movieMutationRepository.clearMovies() }
            verify(exactly = 1) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `direct UID change clears movies before publishing replacement session`() =
        runTest {
            authenticateA()
            val clearMovies = CompletableDeferred<Unit>()
            coEvery { movieMutationRepository.clearMovies() } coAnswers { clearMovies.await() }

            authState.emit(USER_B)
            runCurrent()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.Loading
            verify(exactly = 1) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }

            clearMovies.complete(Unit)
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo authenticated(USER_B)
            coVerify(exactly = 1) { movieMutationRepository.clearMovies() }
            verify(exactly = 2) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 2) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `A to B to A cancels intermediate transition and publishes latest A`() =
        runTest {
            authenticateA()
            var clearCalls = 0
            coEvery { movieMutationRepository.clearMovies() } coAnswers {
                clearCalls += 1
                if (clearCalls == 1) awaitCancellation()
            }

            authState.emit(USER_B)
            runCurrent()
            authState.emit(USER_A)
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo authenticated(USER_A)
            coVerify(exactly = 2) { movieMutationRepository.clearMovies() }
            verify(exactly = 2) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 2) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `duplicate same UID updates user without clear or reschedule`() =
        runTest {
            authenticateA()
            val updatedUser = USER_A.copy(displayName = "Updated")

            authState.emit(updatedUser)
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo authenticated(updatedUser)
            coVerify(exactly = 0) { movieMutationRepository.clearMovies() }
            verify(exactly = 1) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `guest onboarding dismissal updates UI and persists preference`() =
        runTest {
            authState.tryEmit(GUEST_USER)
            buildViewModel()
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo authenticated(GUEST_USER)

            sut.process(MainActivityIntent.DismissGuestOnboarding)
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo
                authenticated(GUEST_USER).copy(showGuestOnboarding = false)
            coVerify(exactly = 1) { setHasSeenGuestOnboardingUseCase(true) }
        }

    @Test
    fun `persisted guest onboarding state updates without restarting session`() =
        runTest {
            val onboardingState = MutableStateFlow(false)
            every { observeHasSeenGuestOnboardingUseCase() } returns onboardingState
            authState.tryEmit(GUEST_USER)
            buildViewModel()
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo authenticated(GUEST_USER)

            onboardingState.value = true
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo
                authenticated(GUEST_USER).copy(showGuestOnboarding = false)
            coVerify(exactly = 0) { movieMutationRepository.clearMovies() }
            verify(exactly = 1) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `cleanup failure remains fail closed and retry succeeds`() =
        runTest {
            coEvery { movieMutationRepository.clearMovies() } throws
                IllegalStateException("database") andThen Unit
            buildViewModel()
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.LocalCleanupError
            sut.process(MainActivityIntent.RetryAccountRecovery)
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.Unauthenticated
            coVerify(exactly = 2) { movieMutationRepository.clearMovies() }
        }

    @Test
    fun `cleanup cancellation keeps graph hidden`() =
        runTest {
            coEvery { movieMutationRepository.clearMovies() } throws CancellationException()

            buildViewModel()
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.Loading
            verify(exactly = 0) { syncScheduler.schedulePeriodicSync() }
        }

    @Test
    fun `FIP 022 marker clears movies before marker and session publication`() =
        runTest {
            authState.tryEmit(USER_A)
            deletionPending.value = true

            buildViewModel()
            advanceUntilIdle()

            coVerifyOrder {
                movieMutationRepository.clearMovies()
                deletionRecoveryRepository.clearLocalCleanupPending()
            }
            sut.uiState.value shouldBeEqualTo authenticated(USER_A)
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }
        }

    @Test
    fun `FIP 022 cleanup failure retains marker and supports retry`() =
        runTest {
            authState.tryEmit(USER_A)
            deletionPending.value = true
            coEvery { movieMutationRepository.clearMovies() } throws
                IllegalStateException("database") andThen Unit
            buildViewModel()
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.AccountDeletionRecoveryError
            deletionPending.value shouldBeEqualTo true
            sut.process(MainActivityIntent.RetryAccountRecovery)
            advanceUntilIdle()

            deletionPending.value shouldBeEqualTo false
            sut.uiState.value shouldBeEqualTo authenticated(USER_A)
        }

    @Test
    fun `FIP 022 marker clear failure remains blocked and supports retry`() =
        runTest {
            authState.tryEmit(USER_A)
            deletionPending.value = true
            coEvery { deletionRecoveryRepository.clearLocalCleanupPending() } returns
                Result.failure(IllegalStateException("preferences")) andThenAnswer {
                    deletionPending.value = false
                    Result.success(Unit)
                }

            buildViewModel()
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.AccountDeletionRecoveryError
            deletionPending.value shouldBeEqualTo true

            sut.process(MainActivityIntent.RetryAccountRecovery)
            advanceUntilIdle()

            deletionPending.value shouldBeEqualTo false
            sut.uiState.value shouldBeEqualTo authenticated(USER_A)
            coVerify(exactly = 2) { movieMutationRepository.clearMovies() }
        }

    @Test
    fun `FIP 022 marker during remote lease preserves graph and defers cleanup`() =
        runTest {
            authenticateA()

            remoteDeletionInFlight.value = true
            deletionPending.value = true
            runCurrent()

            sut.isRemoteAccountDeletionInProgress.value shouldBeEqualTo true
            sut.uiState.value shouldBeEqualTo authenticated(USER_A)
            coVerify(exactly = 0) { movieMutationRepository.clearMovies() }

            remoteDeletionInFlight.value = false
            advanceUntilIdle()

            coVerify(exactly = 1) { movieMutationRepository.clearMovies() }
            coVerify(exactly = 1) { deletionRecoveryRepository.clearLocalCleanupPending() }
            sut.isRemoteAccountDeletionInProgress.value shouldBeEqualTo false
            sut.uiState.value shouldBeEqualTo authenticated(USER_A)
        }

    private suspend fun TestScope.authenticateA() {
        authState.tryEmit(USER_A)
        buildViewModel()
        advanceUntilIdle()
        sut.uiState.value shouldBeEqualTo authenticated(USER_A)
    }

    private fun buildViewModel() {
        sut =
            MainActivityViewModel(
                observeAuthStateUseCase,
                observeHasSeenGuestOnboardingUseCase,
                observeThemeUseCase,
                setHasSeenGuestOnboardingUseCase,
                syncScheduler,
                movieMutationRepository,
                deletionRecoveryRepository,
            )
    }

    private fun authenticated(user: SantoroUser) =
        MainActivityUiState.Authenticated(
            user = user,
            showGuestOnboarding = user.isAnonymous,
        )

    private companion object {
        val USER_A = SantoroUser("account-a", "a@example.com", "A", null, false)
        val USER_B = SantoroUser("account-b", "b@example.com", "B", null, false)
        val GUEST_USER = SantoroUser("guest", null, null, null, true)
    }
}
