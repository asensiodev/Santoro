package com.asensiodev.feature.moviedetail.impl.presentation.component

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.AppIcons
import org.junit.Rule
import org.junit.Test

class AnimatedIconWithTextScreenshotTest {
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
                AnimatedIconWithText(
                    isSelected = true,
                    onClick = {},
                    selectedIcon = AppIcons.Watched,
                    unselectedIcon = AppIcons.Add,
                    label = "Watched",
                )
            }
        }
    }
}
