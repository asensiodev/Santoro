package com.asensiodev.feature.searchmovies.impl.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.feature.searchmovies.impl.domain.model.MovieLibraryStatus
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
                HeroMovieCard(
                    movie =
                        MovieUi(
                            id = 1,
                            title = "The Lord of the Rings: The Fellowship of the Ring",
                            posterPath = null,
                            backdropPath = null,
                            voteAverage = 8.8,
                            libraryStatus = status,
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
