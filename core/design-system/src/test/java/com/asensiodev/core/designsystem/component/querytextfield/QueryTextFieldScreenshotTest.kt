package com.asensiodev.core.designsystem.component.querytextfield

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.PreviewContent
import org.junit.Rule
import org.junit.Test

class QueryTextFieldScreenshotTest {
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
                PreviewContent {
                    QueryTextField(
                        query = "",
                        placeholder = "Type to search movies",
                        onQueryChanged = {},
                    )
                }
            }
        }
    }
}
