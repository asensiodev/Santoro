package com.asensiodev.feature.watchlist.impl.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import org.junit.Rule
import org.junit.Test

class WatchlistMovieItemScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_3,
            theme = "Theme.Santoro",
            renderingMode = SessionParams.RenderingMode.SHRINK,
            showSystemUi = false,
        )

    @Test
    fun captureScreenshot() {
        paparazzi.snapshot {
            PreviewContent {
                WatchlistMovieItem(
                    movie =
                        MovieUi(
                            id = 1,
                            title = "Inception",
                            posterPath = null,
                            releaseYear = "2010",
                            genres = "Science Fiction, Action",
                            rating = 8.8,
                        ),
                    onClick = {},
                    modifier = Modifier.padding(Spacings.spacing16),
                )
            }
        }
    }
}
