package com.asensiodev.core.designsystem.component.bottombar

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.PreviewContent
import org.junit.Rule
import org.junit.Test

class BottomNavigationBarScreenshotTest {
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
                BottomNavigationBar(
                    BottomNavItem.entries,
                    selectedItem = null,
                    onItemSelected = {},
                )
            }
        }
    }
}
