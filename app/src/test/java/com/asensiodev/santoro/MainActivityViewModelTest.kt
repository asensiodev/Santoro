package com.asensiodev.santoro

import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.repository.AccountDeletionRecoveryRepository
import com.asensiodev.core.domain.usecase.ObserveHasSeenGuestOnboardingUseCase
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetHasSeenGuestOnboardingUseCase
import com.asensiodev.core.testing.extension.CoroutineTestExtension
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
    private val setHasSeenGuestOnboardingUseCase: SetHasSeenGuestOnboardingUseCase = mockk()
    private val syncScheduler: WorkManagerSyncScheduler = mockk(relaxed = true)
    private val recoveryRepository: AccountDeletionRecoveryRepository = mockk()
    private val databaseRepository: DatabaseRepository = mockk()
    private val isLocalCleanupPending = MutableStateFlow(false)

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
        every { recoveryRepository.isLocalCleanupPending } returns isLocalCleanupPending
        coEvery { databaseRepository.clearAllUserData() } returns Result.success(Unit)
        coEvery { recoveryRepository.clearLocalCleanupPending() } coAnswers {
            isLocalCleanupPending.value = false
            Result.success(Unit)
        }
    }

    private fun buildViewModel() {
        sut =
            MainActivityViewModel(
                observeAuthStateUseCase = observeAuthStateUseCase,
                observeHasSeenGuestOnboardingUseCase = observeHasSeenGuestOnboardingUseCase,
                observeThemeUseCase = observeThemeUseCase,
                setHasSeenGuestOnboardingUseCase = setHasSeenGuestOnboardingUseCase,
                syncScheduler = syncScheduler,
                recoveryRepository = recoveryRepository,
                databaseRepository = databaseRepository,
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
            backgroundScope.launch { sut.uiState.collect {} }
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
    fun `GIVEN scheduler fails WHEN a later user authenticates THEN schedules sync again`() =
        runTest {
            val authState = MutableSharedFlow<SantoroUser?>()
            every { observeAuthStateUseCase() } returns authState
            every { syncScheduler.schedulePeriodicSync() } throws IllegalStateException() andThen Unit

            buildViewModel()
            advanceUntilIdle()
            authState.emit(anonymousUser)
            advanceUntilIdle()
            authState.emit(googleUser)
            advanceUntilIdle()

            verify(exactly = 2) { syncScheduler.schedulePeriodicSync() }
            verify(exactly = 1) { syncScheduler.scheduleImmediateSync() }
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

    @Test
    fun `GIVEN cleanup pending at startup WHEN recovery succeeds THEN clears data before Login`() =
        runTest {
            isLocalCleanupPending.value = true
            every { observeAuthStateUseCase() } returns flowOf(null)

            buildViewModel()
            backgroundScope.launch { sut.uiState.collect {} }
            advanceUntilIdle()

            coVerify(exactly = 1) { databaseRepository.clearAllUserData() }
            coVerify(exactly = 1) { recoveryRepository.clearLocalCleanupPending() }
            sut.uiState.value shouldBeEqualTo MainActivityUiState.Unauthenticated
        }

    @Test
    fun `GIVEN deletion marker WHEN Auth becomes null THEN blocks Login until cleanup finishes`() =
        runTest {
            val authState = MutableStateFlow<SantoroUser?>(googleUser)
            val cleanupResult = CompletableDeferred<Result<Unit>>()
            every { observeAuthStateUseCase() } returns authState
            coEvery { databaseRepository.clearAllUserData() } coAnswers { cleanupResult.await() }
            buildViewModel()
            backgroundScope.launch { sut.uiState.collect {} }
            advanceUntilIdle()

            isLocalCleanupPending.value = true
            authState.value = null
            runCurrent()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.Loading

            cleanupResult.complete(Result.success(Unit))
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.Unauthenticated
        }

    @Test
    fun `GIVEN cleanup fails WHEN retry succeeds THEN keeps app blocked until marker clears`() =
        runTest {
            isLocalCleanupPending.value = true
            every { observeAuthStateUseCase() } returns flowOf(null)
            coEvery { databaseRepository.clearAllUserData() } returnsMany
                listOf(Result.failure(Exception("Room")), Result.success(Unit))
            buildViewModel()
            backgroundScope.launch { sut.uiState.collect {} }
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.AccountDeletionRecoveryError
            isLocalCleanupPending.value shouldBeEqualTo true

            sut.retryAccountDeletionRecovery()
            advanceUntilIdle()

            sut.uiState.value shouldBeEqualTo MainActivityUiState.Unauthenticated
            coVerify(exactly = 2) { databaseRepository.clearAllUserData() }
        }

    @Test
    fun `GIVEN cleanup is cancelled WHEN recovering THEN marker is retained`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            isLocalCleanupPending.value = true
            every { observeAuthStateUseCase() } returns flowOf(null)
            coEvery { databaseRepository.clearAllUserData() } returns Result.failure(cancellation)

            buildViewModel()
            runCurrent()

            isLocalCleanupPending.value shouldBeEqualTo true
            coVerify(exactly = 0) { recoveryRepository.clearLocalCleanupPending() }
        }
}
