package com.asensiodev.feature.watchlist.impl.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

@RunWith(Parameterized::class)
class WatchlistMoviesScreenScreenshotTest(
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
                "content" ->
                    WatchlistMoviesUiState(
                        screenState = WatchlistScreenState.Content,
                        movies = listOf(MovieUi(1, "The Long Journey", null, "2024", "Drama", 8.5)),
                        totalMoviesCount = 1,
                        hasMovies = true,
                        listHeader = WatchlistListHeaderUi.MoviesToWatch(1),
                    )
                "loading" -> WatchlistMoviesUiState()
                "empty" -> WatchlistMoviesUiState(screenState = WatchlistScreenState.Empty, hasMovies = false)
                "noResults" -> WatchlistMoviesUiState(screenState = WatchlistScreenState.NoResults, query = "Unknown")
                "error" -> WatchlistMoviesUiState(screenState = WatchlistScreenState.Error("Unavailable"))
                else -> error("Unknown scenario")
            }
        paparazzi.snapshot {
            SantoroTheme(darkTheme = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WatchlistMoviesScreen(state, {}, {})
                }
            }
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}-dark={1}")
        fun scenarios(): List<Array<Any>> =
            listOf("content", "loading", "empty", "noResults", "error").flatMap { scenario ->
                listOf(arrayOf(scenario, false), arrayOf(scenario, true))
            }
    }
}
