package com.asensiodev.feature.watchedmovies.impl.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import org.junit.Rule
import org.junit.Test

class MovieCardScreenshotTest {
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
                MovieCard(
                    movie =
                        MovieUi(
                            id = 1,
                            title = "Sample Movie",
                            posterPath = null,
                            watchedDate = "January 2024",
                        ),
                    onClick = {},
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .size(width = 120.dp, height = 180.dp),
                )
            }
        }
    }
}
