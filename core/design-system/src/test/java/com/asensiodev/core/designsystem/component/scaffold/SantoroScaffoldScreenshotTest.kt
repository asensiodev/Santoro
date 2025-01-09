package com.asensiodev.core.designsystem.component.scaffold

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.component.bottombar.BottomNavItem
import com.asensiodev.core.designsystem.component.topbar.TopAppBar
import org.junit.Rule
import org.junit.Test

class SantoroScaffoldScreenshotTest {
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
                SantoroScaffold(
                    topBar = {
                        TopAppBar(
                            title = "Santoro Movies",
                            onBackClick = {},
                        )
                    },
                    bottomNavItems = BottomNavItem.entries,
                    selectedBottomNavItem = null,
                    onBottomNavItemSelected = {},
                ) {
                    TestScreen()
                }
            }
        }
    }
}
