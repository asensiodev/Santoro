package com.asensiodev.feature.watchlist.impl.presentation.component

import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
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
class WatchlistSurpriseContentScreenshotTest(
    private val hasAlternatives: Boolean,
    private val dark: Boolean,
) {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_3,
            theme = "Theme.Santoro",
            renderingMode = SessionParams.RenderingMode.SHRINK,
            showSystemUi = false,
        )
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
        paparazzi.snapshot {
            SantoroTheme(darkTheme = dark) {
                Surface {
                    WatchlistSurpriseContent(
                        movie = MovieUi(1, "The Long Journey", null, "2024", "Drama", 8.5),
                        hasAlternatives = hasAlternatives,
                        onViewDetails = {},
                        onPickAnother = {},
                    )
                }
            }
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "alternatives={0}-dark={1}")
        fun scenarios(): List<Array<Any>> =
            listOf(false, true).flatMap { alternatives ->
                listOf(arrayOf(alternatives, false), arrayOf(alternatives, true))
            }
    }
}
