package com.asensiodev.settings.impl.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignOutUseCase
import com.asensiodev.core.domain.model.AppLanguage
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetThemeUseCase
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import com.asensiodev.settings.impl.domain.usecase.DeleteAccountUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val observeAuthStateUseCase: ObserveAuthStateUseCase = mockk(relaxed = true)
    private val signOutUseCase: SignOutUseCase = mockk(relaxed = true)
    private val deleteAccountUseCase: DeleteAccountUseCase = mockk(relaxed = true)
    private val observeThemeUseCase: ObserveThemeUseCase = mockk()
    private val setThemeUseCase: SetThemeUseCase = mockk(relaxed = true)
    private val databaseRepository: DatabaseRepository = mockk(relaxed = true)

    private lateinit var sut: SettingsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeThemeUseCase() } returns flowOf(ThemeOption.SYSTEM)
        sut =
            SettingsViewModel(
                observeAuthStateUseCase = observeAuthStateUseCase,
                signOutUseCase = signOutUseCase,
                deleteAccountUseCase = deleteAccountUseCase,
                observeThemeUseCase = observeThemeUseCase,
                setThemeUseCase = setThemeUseCase,
                databaseRepository = databaseRepository,
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
    fun `GIVEN OnLogoutClicked WHEN process THEN clears data and signs out`() =
        runTest {
            sut.process(SettingsIntent.OnLogoutClicked)
            advanceUntilIdle()

            coVerify(exactly = 1) { databaseRepository.clearAllUserData() }
            coVerify(exactly = 1) { signOutUseCase() }
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
            // GIVEN
            coEvery { deleteAccountUseCase() } returns Result.success(Unit)
            sut.process(SettingsIntent.OnDeleteAccountClicked)

            // WHEN
            sut.process(SettingsIntent.ConfirmDeleteAccount)

            // THEN
            sut.uiState.value.showDeleteAccountDialog shouldBeEqualTo false
            sut.uiState.value.isLoading shouldBeEqualTo true
        }

    @Test
    fun `GIVEN ConfirmDeleteAccount WHEN success THEN isLoading is false and no error`() =
        runTest {
            // GIVEN
            coEvery { deleteAccountUseCase() } returns Result.success(Unit)

            // WHEN
            sut.process(SettingsIntent.ConfirmDeleteAccount)
            advanceUntilIdle()

            // THEN
            sut.uiState.value.isLoading shouldBeEqualTo false
            sut.uiState.value.error shouldBeEqualTo null
        }

    @Test
    fun `GIVEN ConfirmDeleteAccount WHEN success THEN delegates to use case`() =
        runTest {
            // GIVEN
            coEvery { deleteAccountUseCase() } returns Result.success(Unit)

            // WHEN
            sut.process(SettingsIntent.ConfirmDeleteAccount)
            advanceUntilIdle()

            // THEN
            coVerify(exactly = 1) { deleteAccountUseCase() }
        }

    @Test
    fun `GIVEN ConfirmDeleteAccount WHEN failure THEN isLoading is false and error is set`() =
        runTest {
            // GIVEN
            coEvery { deleteAccountUseCase() } returns Result.failure(Exception("error"))

            // WHEN
            sut.process(SettingsIntent.ConfirmDeleteAccount)
            advanceUntilIdle()

            // THEN
            sut.uiState.value.isLoading shouldBeEqualTo false
            sut.uiState.value.error
                .shouldNotBeNull()
        }

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
}
