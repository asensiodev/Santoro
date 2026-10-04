package com.asensiodev.feature.searchmovies.impl.presentation.seeall

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import com.asensiodev.ui.UiText
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

@RunWith(Parameterized::class)
class SeeAllMoviesScreenScreenshotTest(
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
                "content", "stale" ->
                    SeeAllMoviesUiState(
                        screenState = SeeAllScreenState.Content,
                        movies = listOf(MovieUi(1, "The Long Journey", null, null, 8.5)),
                        isEndReached = true,
                        isShowingStaleData = scenario == "stale",
                    )
                "loading" -> SeeAllMoviesUiState()
                "empty" -> SeeAllMoviesUiState(screenState = SeeAllScreenState.Empty)
                "error" ->
                    SeeAllMoviesUiState(
                        screenState =
                            SeeAllScreenState.Error(
                                UiText.DynamicString("Could not load movies. Try again."),
                            ),
                    )
                else -> error("Unknown scenario")
            }
        paparazzi.snapshot {
            SantoroTheme(darkTheme = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SeeAllMoviesScreen(state, {}, {})
                }
            }
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}-dark={1}")
        fun scenarios(): List<Array<Any>> =
            listOf("content", "loading", "empty", "error", "stale").flatMap { scenario ->
                listOf(arrayOf(scenario, false), arrayOf(scenario, true))
            }
    }
}
