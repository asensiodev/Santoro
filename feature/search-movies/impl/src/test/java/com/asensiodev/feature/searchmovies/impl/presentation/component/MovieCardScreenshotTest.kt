package com.asensiodev.feature.searchmovies.impl.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.searchmovies.impl.domain.model.MovieLibraryStatus
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
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
        capture(status = null)
    }

    @Test
    fun captureNoneDark() {
        capture(status = null, darkTheme = true)
    }

    @Test
    fun captureWatchedLight() {
        capture(status = MovieLibraryStatus.Watched)
    }

    @Test
    fun captureWatchedDark() {
        capture(status = MovieLibraryStatus.Watched, darkTheme = true)
    }

    @Test
    fun captureWatchlistLight() {
        capture(status = MovieLibraryStatus.Watchlist)
    }

    @Test
    fun captureWatchlistDark() {
        capture(status = MovieLibraryStatus.Watchlist, darkTheme = true)
    }

    private fun capture(
        status: MovieLibraryStatus?,
        darkTheme: Boolean = false,
    ) {
        paparazzi.snapshot {
            ScreenshotContent(darkTheme) {
                MovieCard(
                    movie =
                        MovieUi(
                            id = 1,
                            title = "Sample Movie",
                            posterPath = null,
                            backdropPath = null,
                            voteAverage = 10.0,
                            libraryStatus = status,
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

    @Composable
    private fun ScreenshotContent(
        darkTheme: Boolean,
        content: @Composable () -> Unit,
    ) {
        SantoroTheme(darkTheme = darkTheme) {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                content()
            }
        }
    }
}
