package com.asensiodev.feature.watchedmovies.impl.presentation.component

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.feature.watchedmovies.impl.domain.model.WatchedStats
import org.junit.Rule
import org.junit.Test

class WatchedStatsDashboardScreenshotTest {
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
                WatchedStatsDashboard(
                    stats =
                        WatchedStats(
                            totalWatched = 42,
                            totalRuntimeHours = 84,
                            favouriteGenre = "Science Fiction",
                            longestStreakWeeks = 5,
                        ),
                )
            }
        }
    }
}
