package com.asensiodev.core.designsystem.component.scaffold

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.component.bottombar.BottomNavItem
import com.asensiodev.core.designsystem.component.bottombar.BottomNavigationBar
import com.asensiodev.core.designsystem.component.topbar.SantoroTopAppBar
import com.asensiodev.core.designsystem.theme.AppIcons
import org.junit.Rule
import org.junit.Test
import com.asensiodev.santoro.core.stringresources.R as SR

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
                        SantoroTopAppBar(
                            title = "Santoro Movies",
                            onBackClick = {},
                        )
                    },
                    bottomBar = {
                        BottomNavigationBar(
                            items =
                                listOf(
                                    BottomNavItem(
                                        icon = AppIcons.SearchIcon,
                                        labelRes = SR.string.search_movies,
                                        isSelected = true,
                                        onClick = {},
                                    ),
                                    BottomNavItem(
                                        icon = AppIcons.WatchedMoviesIcon,
                                        labelRes = SR.string.watched_movies,
                                        isSelected = false,
                                        onClick = {},
                                    ),
                                    BottomNavItem(
                                        icon = AppIcons.WatchlistIcon,
                                        labelRes = SR.string.watchlist,
                                        isSelected = false,
                                        onClick = {},
                                    ),
                                ),
                        )
                    },
                ) {
                    TestScreen()
                }
            }
        }
    }
}
