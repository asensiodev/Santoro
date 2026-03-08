package com.asensiodev.feature.searchmovies.impl.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import org.junit.Rule
import org.junit.Test

class HeroMovieCardScreenshotTest {
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
                HeroMovieCard(
                    movie =
                        MovieUi(
                            id = 1,
                            title = "The Lord of the Rings: The Fellowship of the Ring",
                            posterPath = null,
                            backdropPath = null,
                            voteAverage = 8.8,
                        ),
                    onClick = {},
                    modifier =
                        Modifier
                            .height(Size.size200)
                            .fillMaxWidth(),
                )
            }
        }
    }
}
