package com.asensiodev.core.designsystem.component.bottombar

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.AppIcons
import org.junit.Rule
import org.junit.Test
import com.asensiodev.santoro.core.stringresources.R as SR

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
                    items =
                        listOf(
                            BottomNavItem(
                                selectedIcon = AppIcons.Home,
                                unselectedIcon = AppIcons.HomeOutlined,
                                labelRes = SR.string.search_movies,
                                isSelected = true,
                                onClick = {},
                            ),
                            BottomNavItem(
                                selectedIcon = AppIcons.Watched,
                                unselectedIcon = AppIcons.WatchedOutlined,
                                labelRes = SR.string.watched_movies,
                                isSelected = false,
                                onClick = {},
                            ),
                            BottomNavItem(
                                selectedIcon = AppIcons.Watchlist,
                                unselectedIcon = AppIcons.WatchlistOutlined,
                                labelRes = SR.string.watchlist,
                                isSelected = false,
                                onClick = {},
                            ),
                        ),
                )
            }
        }
    }
}
