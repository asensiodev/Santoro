package com.asensiodev.settings.impl.presentation.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignOutUseCase
import com.asensiodev.auth.helper.GoogleSignInHelper
import com.asensiodev.core.domain.model.AppLanguage
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetThemeUseCase
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import com.asensiodev.settings.impl.domain.usecase.DeleteAccountUseCase
import com.asensiodev.ui.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale
import com.asensiodev.santoro.core.stringresources.R as SR

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val observeAuthStateUseCase: ObserveAuthStateUseCase = mockk(relaxed = true)
    private val signOutUseCase: SignOutUseCase = mockk(relaxed = true)
    private val deleteAccountUseCase: DeleteAccountUseCase = mockk(relaxed = true)
    private val observeThemeUseCase: ObserveThemeUseCase = mockk()
    private val setThemeUseCase: SetThemeUseCase = mockk(relaxed = true)
    private val syncRepository: SyncRepository = mockk(relaxed = true)
    private val googleSignInHelper: GoogleSignInHelper = mockk()
    private val context: Context = mockk(relaxed = true)

    private lateinit var sut: SettingsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeThemeUseCase() } returns flowOf(ThemeOption.SYSTEM)
        every { observeAuthStateUseCase() } returns flowOf(null)
        coEvery { syncRepository.uploadPendingChanges(any()) } returns Result.success(Unit)
        coEvery { googleSignInHelper.signIn(context) } returns Result.success(ID_TOKEN)
        sut =
            SettingsViewModel(
                observeAuthStateUseCase = observeAuthStateUseCase,
                signOutUseCase = signOutUseCase,
                deleteAccountUseCase = deleteAccountUseCase,
                observeThemeUseCase = observeThemeUseCase,
                setThemeUseCase = setThemeUseCase,
                syncRepository = syncRepository,
                googleSignInHelper = googleSignInHelper,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN onAppearanceClicked WHEN called THEN showThemePicker is true`() =
        runTest {
            sut.process(SettingsIntent.OnAppearanceClicked)

            sut.uiState.value.showThemePicker shouldBeEqualTo true
        }

    @Test
    fun `GIVEN showThemePicker is true WHEN dismissThemePicker THEN showThemePicker is false`() =
        runTest {
            sut.process(SettingsIntent.OnAppearanceClicked)
            sut.process(SettingsIntent.DismissThemePicker)

            sut.uiState.value.showThemePicker shouldBeEqualTo false
        }

    @Test
    fun `GIVEN setTheme DARK WHEN called THEN delegates to use case with DARK`() =
        runTest {
            sut.process(SettingsIntent.SetTheme(ThemeOption.DARK))
            advanceUntilIdle()

            coVerify(exactly = 1) { setThemeUseCase(ThemeOption.DARK) }
        }

    @Test
    fun `GIVEN setTheme called WHEN completed THEN showThemePicker is false`() =
        runTest {
            sut.process(SettingsIntent.OnAppearanceClicked)
            sut.process(SettingsIntent.SetTheme(ThemeOption.LIGHT))
            advanceUntilIdle()

            sut.uiState.value.showThemePicker shouldBeEqualTo false
        }

    @Test
    fun `GIVEN OnLanguageClicked WHEN process THEN showLanguagePicker is true`() =
        runTest {
            sut.process(SettingsIntent.OnLanguageClicked)

            sut.uiState.value.showLanguagePicker shouldBeEqualTo true
        }

    @Test
    fun `GIVEN SetLanguage Spanish WHEN process THEN showLanguagePicker is false`() =
        runTest {
            sut.process(SettingsIntent.OnLanguageClicked)
            sut.process(SettingsIntent.SetLanguage(AppLanguage.SPANISH))

            sut.uiState.value.showLanguagePicker shouldBeEqualTo false
        }

    @Test
    fun `GIVEN DismissLanguagePicker WHEN process THEN showLanguagePicker is false`() =
        runTest {
            sut.process(SettingsIntent.OnLanguageClicked)
            sut.process(SettingsIntent.DismissLanguagePicker)

            sut.uiState.value.showLanguagePicker shouldBeEqualTo false
        }

    @Test
    fun `GIVEN OnLogoutClicked WHEN process THEN signs out without clearing data`() =
        runTest {
            sut.process(SettingsIntent.OnLogoutClicked)
            advanceUntilIdle()

            coVerify(exactly = 1) { signOutUseCase() }
        }

    @Test
    fun `GIVEN Google user WHEN OnLogoutClicked THEN uploads changes before signing out`() =
        runTest {
            val user = SantoroUser("uid123", "test@email.com", null, null, false)
            every { observeAuthStateUseCase() } returns flowOf(user)
            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()

            sut.process(SettingsIntent.OnLogoutClicked)
            advanceUntilIdle()

            coVerify(exactly = 1) { syncRepository.uploadPendingChanges("uid123") }
            coVerify(exactly = 1) { signOutUseCase() }
        }

    @Test
    fun `GIVEN Google user and sync failure WHEN OnLogoutClicked THEN keeps data and does not sign out`() =
        runTest {
            val user = SantoroUser("uid123", "test@email.com", null, null, false)
            every { observeAuthStateUseCase() } returns flowOf(user)
            coEvery { syncRepository.uploadPendingChanges("uid123") } returns Result.failure(Exception())
            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()

            sut.process(SettingsIntent.OnLogoutClicked)
            advanceUntilIdle()

            coVerify(exactly = 0) { signOutUseCase() }
        }

    @Test
    fun `GIVEN auth observation is active WHEN ObserveAuth repeats THEN subscribes once`() =
        runTest {
            var subscriptions = 0
            every { observeAuthStateUseCase() } returns
                flow {
                    subscriptions++
                    awaitCancellation()
                }

            sut.process(SettingsIntent.ObserveAuth)
            sut.process(SettingsIntent.ObserveAuth)
            runCurrent()

            subscriptions shouldBeEqualTo 1
        }

    @Test
    fun `GIVEN theme observation is active WHEN ObserveTheme repeats THEN subscribes once`() =
        runTest {
            var subscriptions = 0
            every { observeThemeUseCase() } returns
                flow {
                    subscriptions++
                    awaitCancellation()
                }

            sut.process(SettingsIntent.ObserveTheme)
            sut.process(SettingsIntent.ObserveTheme)
            runCurrent()

            subscriptions shouldBeEqualTo 1
        }

    @Test
    fun `GIVEN auth has not emitted WHEN state is read THEN account actions are hidden`() =
        runTest {
            sut.uiState.value.showAccountActions shouldBeEqualTo false
        }

    @Test
    fun `GIVEN known Google user WHEN auth emits THEN account actions are shown`() =
        runTest {
            val user = SantoroUser("uid123", "test@email.com", null, null, false)
            every { observeAuthStateUseCase() } returns flowOf(user)

            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()

            sut.uiState.value.showAccountActions shouldBeEqualTo true
        }

    @Test
    fun `GIVEN logout starts WHEN intent is processed THEN loading is set synchronously`() =
        runTest {
            sut.process(SettingsIntent.OnLogoutClicked)

            sut.uiState.value.isLoading shouldBeEqualTo true
        }

    @Test
    fun `GIVEN logout is pending WHEN destructive intents repeat THEN destructive work runs once`() =
        runTest {
            val user = SantoroUser("uid123", "test@email.com", null, null, false)
            val releaseSync = CompletableDeferred<Unit>()
            every { observeAuthStateUseCase() } returns flowOf(user)
            coEvery { syncRepository.uploadPendingChanges("uid123") } coAnswers {
                releaseSync.await()
                Result.success(Unit)
            }
            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()

            sut.process(SettingsIntent.OnLogoutClicked)
            sut.process(SettingsIntent.OnLogoutClicked)
            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            runCurrent()

            coVerify(exactly = 1) { syncRepository.uploadPendingChanges("uid123") }
            coVerify(exactly = 0) { deleteAccountUseCase(any(), any()) }

            releaseSync.complete(Unit)
            advanceUntilIdle()
        }

    @Test
    fun `GIVEN credential request is pending WHEN deletion repeats THEN credential work runs once`() =
        runTest {
            val releaseCredential = CompletableDeferred<Result<String>>()
            coEvery { googleSignInHelper.signIn(context) } coAnswers { releaseCredential.await() }

            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            runCurrent()

            coVerify(exactly = 1) { googleSignInHelper.signIn(context) }
            coVerify(exactly = 0) { deleteAccountUseCase(any(), any()) }

            releaseCredential.complete(Result.failure(Exception()))
            advanceUntilIdle()
        }

    @Test
    fun `GIVEN account deletion is pending WHEN destructive intents repeat THEN destructive work runs once`() =
        runTest {
            val releaseDeletion = CompletableDeferred<Unit>()
            every { observeAuthStateUseCase() } returns flowOf(GOOGLE_USER)
            coEvery { deleteAccountUseCase(UID, ID_TOKEN) } coAnswers {
                releaseDeletion.await()
                Result.success(Unit)
            }
            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()

            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            sut.process(SettingsIntent.OnLogoutClicked)
            runCurrent()

            coVerify(exactly = 1) { googleSignInHelper.signIn(context) }
            coVerify(exactly = 1) { deleteAccountUseCase(UID, ID_TOKEN) }
            coVerify(exactly = 0) { signOutUseCase() }

            releaseDeletion.complete(Unit)
            advanceUntilIdle()
        }

    @Test
    fun `GIVEN logout fails WHEN work ends THEN sets localized pending message`() =
        runTest {
            coEvery { signOutUseCase() } throws Exception()

            sut.process(SettingsIntent.OnLogoutClicked)
            advanceUntilIdle()

            pendingMessageResId() shouldBeEqualTo SR.string.settings_logout_error
        }

    @Test
    fun `GIVEN deletion fails WHEN error is acknowledged THEN pending message is cleared`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(GOOGLE_USER)
            coEvery { deleteAccountUseCase(UID, ID_TOKEN) } returns Result.failure(Exception())
            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()

            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            advanceUntilIdle()

            val pendingMessage = requireNotNull(sut.uiState.value.pendingMessage)
            pendingMessageResId() shouldBeEqualTo SR.string.settings_delete_account_error

            sut.process(SettingsIntent.ErrorShown(pendingMessage.id))

            sut.uiState.value.pendingMessage shouldBeEqualTo null
        }

    @Test
    fun `GIVEN logout is cancelled WHEN work ends THEN loading resets without pending message`() =
        runTest {
            coEvery { signOutUseCase() } throws CancellationException()

            sut.process(SettingsIntent.OnLogoutClicked)
            advanceUntilIdle()

            sut.uiState.value.isLoading shouldBeEqualTo false
            sut.uiState.value.pendingMessage shouldBeEqualTo null
        }

    @Test
    fun `GIVEN OnDeleteAccountClicked WHEN process THEN showDeleteAccountDialog is true`() =
        runTest {
            sut.process(SettingsIntent.OnDeleteAccountClicked)

            sut.uiState.value.showDeleteAccountDialog shouldBeEqualTo true
        }

    @Test
    fun `GIVEN DismissDeleteAccountDialog WHEN process THEN showDeleteAccountDialog is false`() =
        runTest {
            sut.process(SettingsIntent.OnDeleteAccountClicked)
            sut.process(SettingsIntent.DismissDeleteAccountDialog)

            sut.uiState.value.showDeleteAccountDialog shouldBeEqualTo false
        }

    @Test
    fun `GIVEN ConfirmDeleteAccount WHEN process THEN dialog is dismissed and loading starts`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(GOOGLE_USER)
            coEvery { deleteAccountUseCase(UID, ID_TOKEN) } returns Result.success(Unit)
            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()
            sut.process(SettingsIntent.OnDeleteAccountClicked)

            sut.process(SettingsIntent.ConfirmDeleteAccount(context))

            sut.uiState.value.showDeleteAccountDialog shouldBeEqualTo false
            sut.uiState.value.isLoading shouldBeEqualTo true
        }

    @Test
    fun `GIVEN Google user and credential WHEN confirmed THEN delegates uid and token`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(GOOGLE_USER)
            coEvery { deleteAccountUseCase(UID, ID_TOKEN) } returns Result.success(Unit)
            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()

            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            advanceUntilIdle()

            coVerify(exactly = 1) { googleSignInHelper.signIn(context) }
            coVerify(exactly = 1) { deleteAccountUseCase(UID, ID_TOKEN) }
            sut.uiState.value.isLoading shouldBeEqualTo false
        }

    @Test
    fun `GIVEN credential request fails WHEN confirmed THEN sets deletion error and does not delete`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(GOOGLE_USER)
            coEvery { googleSignInHelper.signIn(context) } returns Result.failure(Exception())
            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()

            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            advanceUntilIdle()

            pendingMessageResId() shouldBeEqualTo SR.string.settings_delete_account_error
            coVerify(exactly = 0) { deleteAccountUseCase(any(), any()) }
        }

    @Test
    fun `GIVEN current user is missing WHEN credential succeeds THEN sets deletion error and does not delete`() =
        runTest {
            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            advanceUntilIdle()

            pendingMessageResId() shouldBeEqualTo SR.string.settings_delete_account_error
            coVerify(exactly = 1) { googleSignInHelper.signIn(context) }
            coVerify(exactly = 0) { deleteAccountUseCase(any(), any()) }
        }

    @Test
    fun `GIVEN current user is anonymous WHEN credential succeeds THEN does not delete`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(ANONYMOUS_USER)
            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()

            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            advanceUntilIdle()

            pendingMessageResId() shouldBeEqualTo SR.string.settings_delete_account_error
            coVerify(exactly = 0) { deleteAccountUseCase(any(), any()) }
        }

    @Test
    fun `GIVEN deletion fails WHEN confirmed THEN loading resets and deletion error is pending`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(GOOGLE_USER)
            coEvery { deleteAccountUseCase(UID, ID_TOKEN) } returns Result.failure(Exception("error"))
            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()

            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            advanceUntilIdle()

            sut.uiState.value.isLoading shouldBeEqualTo false
            pendingMessageResId() shouldBeEqualTo SR.string.settings_delete_account_error
        }

    @Test
    fun `GIVEN deletion is cancelled WHEN confirmed THEN loading resets without error`() =
        runTest {
            every { observeAuthStateUseCase() } returns flowOf(GOOGLE_USER)
            coEvery { deleteAccountUseCase(UID, ID_TOKEN) } throws CancellationException()
            sut.process(SettingsIntent.ObserveAuth)
            advanceUntilIdle()

            sut.process(SettingsIntent.ConfirmDeleteAccount(context))
            advanceUntilIdle()

            sut.uiState.value.isLoading shouldBeEqualTo false
            sut.uiState.value.pendingMessage shouldBeEqualTo null
        }

    @Test
    fun `GIVEN newer pending error WHEN older error is acknowledged THEN newer error remains`() =
        runTest {
            coEvery { signOutUseCase() } throws Exception()
            sut.process(SettingsIntent.OnLogoutClicked)
            advanceUntilIdle()
            val firstMessage = requireNotNull(sut.uiState.value.pendingMessage)

            sut.process(SettingsIntent.ErrorShown(firstMessage.id))

            sut.process(SettingsIntent.OnLogoutClicked)
            advanceUntilIdle()
            val secondMessage = requireNotNull(sut.uiState.value.pendingMessage)

            sut.process(SettingsIntent.ErrorShown(firstMessage.id))

            sut.uiState.value.pendingMessage shouldBeEqualTo secondMessage
        }

    private fun pendingMessageResId(): Int =
        (requireNotNull(sut.uiState.value.pendingMessage).message as UiText.StringResource).resId

    @Nested
    inner class ResolveCurrentLanguageTest {
        @AfterEach
        fun tearDownStatic() {
            unmockkStatic(AppCompatDelegate::class)
        }

        @Test
        fun `GIVEN per-app locale is es WHEN resolveCurrentLanguage THEN returns SPANISH`() {
            mockkStatic(AppCompatDelegate::class)
            every {
                AppCompatDelegate.getApplicationLocales()
            } returns LocaleListCompat.forLanguageTags("es")

            val result = SettingsViewModel.resolveCurrentLanguage()

            result shouldBeEqualTo AppLanguage.SPANISH
        }

        @Test
        fun `GIVEN per-app locale is en WHEN resolveCurrentLanguage THEN returns ENGLISH`() {
            mockkStatic(AppCompatDelegate::class)
            every {
                AppCompatDelegate.getApplicationLocales()
            } returns LocaleListCompat.forLanguageTags("en")

            val result = SettingsViewModel.resolveCurrentLanguage()

            result shouldBeEqualTo AppLanguage.ENGLISH
        }

        @Test
        fun `GIVEN no per-app locale and device is es WHEN resolveCurrentLanguage THEN returns SPANISH`() {
            mockkStatic(AppCompatDelegate::class)
            every {
                AppCompatDelegate.getApplicationLocales()
            } returns LocaleListCompat.getEmptyLocaleList()
            val original = Locale.getDefault()
            Locale.setDefault(Locale("es"))

            val result = SettingsViewModel.resolveCurrentLanguage()

            result shouldBeEqualTo AppLanguage.SPANISH
            Locale.setDefault(original)
        }

        @Test
        fun `GIVEN no per-app locale and device is fr WHEN resolveCurrentLanguage THEN returns ENGLISH`() {
            mockkStatic(AppCompatDelegate::class)
            every {
                AppCompatDelegate.getApplicationLocales()
            } returns LocaleListCompat.getEmptyLocaleList()
            val original = Locale.getDefault()
            Locale.setDefault(Locale.FRENCH)

            val result = SettingsViewModel.resolveCurrentLanguage()

            result shouldBeEqualTo AppLanguage.ENGLISH
            Locale.setDefault(original)
        }

        @Test
        fun `GIVEN no per-app locale and device is en WHEN resolveCurrentLanguage THEN returns ENGLISH`() {
            mockkStatic(AppCompatDelegate::class)
            every {
                AppCompatDelegate.getApplicationLocales()
            } returns LocaleListCompat.getEmptyLocaleList()
            val original = Locale.getDefault()
            Locale.setDefault(Locale.ENGLISH)

            val result = SettingsViewModel.resolveCurrentLanguage()

            result shouldBeEqualTo AppLanguage.ENGLISH
            Locale.setDefault(original)
        }
    }

    private companion object {
        const val UID = "uid123"
        const val ID_TOKEN = "id-token"
        val GOOGLE_USER = SantoroUser(UID, "test@email.com", null, null, false)
        val ANONYMOUS_USER = SantoroUser(UID, null, null, null, true)
    }
}
