package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi
import com.asensiodev.ui.UiText
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

@RunWith(Parameterized::class)
class MovieDetailScreenScreenshotTest(
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
                    MovieDetailUiState(
                        screenState = MovieDetailScreenState.Content,
                        movie =
                            MovieUi(
                                id = 1,
                                title = "The Long Journey",
                                overview = "A journey across a changing world.",
                                posterPath = null,
                                releaseDate = "2024-03-15",
                                popularity = 10.0,
                                voteAverage = 8.5,
                                voteCount = 100,
                                genres = emptyList(),
                                productionCountries = listOf("Spain"),
                                cast = emptyList(),
                                runtime = "1h 45m",
                                director = "Alex Morgan",
                                isWatched = false,
                                isInWatchlist = false,
                            ),
                    )
                "loading" -> MovieDetailUiState()
                "error" ->
                    MovieDetailUiState(
                        screenState =
                            MovieDetailScreenState.Error(
                                UiText.DynamicString("Could not load this movie. Try again."),
                            ),
                    )
                else -> error("Unknown scenario")
            }
        paparazzi.snapshot {
            SantoroTheme(darkTheme = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MovieDetailScreen(state, {}, {}, {}, {}, {}, {})
                }
            }
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}-dark={1}")
        fun scenarios(): List<Array<Any>> =
            listOf("content", "loading", "error").flatMap { scenario ->
                listOf(arrayOf(scenario, false), arrayOf(scenario, true))
            }
    }
}
