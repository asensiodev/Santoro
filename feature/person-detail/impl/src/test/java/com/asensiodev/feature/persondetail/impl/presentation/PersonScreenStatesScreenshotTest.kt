package com.asensiodev.feature.persondetail.impl.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.domain.model.Person
import com.asensiodev.feature.persondetail.api.navigation.FilmographySection
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

@RunWith(Parameterized::class)
class PersonScreenStatesScreenshotTest(
    private val scenario: String,
    private val dark: Boolean,
) {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_3, theme = "Theme.Santoro", showSystemUi = false)
    private var previousLocale = Locale.getDefault()

    @Before
    fun setLocale() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(previousLocale)
    }

    @Test
    fun capture() {
        val state =
            when (scenario) {
                "profileLoading" -> PersonDetailUiState()
                "profileError" -> PersonDetailUiState(profileState = PersonLoadState.Error)
                "listLoading", "listError" ->
                    PersonDetailUiState(
                        person = Person(1, "Alex Morgan", "A short biography.", null, null, null, null),
                        profileState = PersonLoadState.Content,
                        filmographyState =
                            if (scenario ==
                                "listError"
                            ) {
                                PersonLoadState.Error
                            } else {
                                PersonLoadState.Loading
                            },
                    )
                "profileEmpty", "listEmpty" ->
                    PersonDetailUiState(
                        person = Person(1, "Alex Morgan", "A short biography.", null, null, null, null),
                        profileState = PersonLoadState.Content,
                        filmographyState = PersonLoadState.Content,
                    )
                else -> error("Unknown scenario")
            }
        paparazzi.snapshot {
            SantoroTheme(darkTheme = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (scenario.startsWith("list")) {
                        PersonFilmographyScreen(state, FilmographySection.ACTING, {}, {}, {})
                    } else {
                        PersonDetailScreen(state, {}, {}, {}, {})
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}-dark={1}")
        fun scenarios(): List<Array<Any>> =
            listOf(
                "profileLoading",
                "profileError",
                "profileEmpty",
                "listLoading",
                "listError",
                "listEmpty",
            ).flatMap { scenario ->
                listOf(arrayOf(scenario, false), arrayOf(scenario, true))
            }
    }
}
