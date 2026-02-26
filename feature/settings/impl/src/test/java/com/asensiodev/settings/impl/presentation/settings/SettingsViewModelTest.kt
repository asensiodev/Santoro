package com.asensiodev.settings.impl.presentation.settings

import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignOutUseCase
import com.asensiodev.core.domain.model.AppLanguage
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.usecase.ObserveThemeUseCase
import com.asensiodev.core.domain.usecase.SetThemeUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val observeAuthStateUseCase: ObserveAuthStateUseCase = mockk(relaxed = true)
    private val signOutUseCase: SignOutUseCase = mockk(relaxed = true)
    private val observeThemeUseCase: ObserveThemeUseCase = mockk()
    private val setThemeUseCase: SetThemeUseCase = mockk(relaxed = true)

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
                observeThemeUseCase = observeThemeUseCase,
                setThemeUseCase = setThemeUseCase,
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
}
