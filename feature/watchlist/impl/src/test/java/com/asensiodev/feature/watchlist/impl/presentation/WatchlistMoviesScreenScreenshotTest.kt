package com.asensiodev.feature.watchlist.impl.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.asensiodev.core.designsystem.component.bottombar.BottomNavItem
import com.asensiodev.core.designsystem.component.bottombar.BottomNavigationBar
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale
import com.asensiodev.santoro.core.stringresources.R as SR

@RunWith(Parameterized::class)
class WatchlistMoviesScreenScreenshotTest(
    private val scenario: String,
    private val dark: Boolean,
) {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig =
                DeviceConfig.PIXEL_3.copy(
                    locale = if (scenario.startsWith("contentSpanishLarge")) "es" else "en",
                ),
            theme = "Theme.Santoro",
            showSystemUi = false,
        )
    private var previousLocale = Locale.getDefault()

    @Before
    fun setLocale() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(previousLocale)
    }

    @Test
    fun capture() {
        val movies =
            when {
                scenario.contains("Long") ->
                    List(20) { index ->
                        MovieUi(index, "Saved movie ${index + 1}", null, "2024", "Drama", 8.5)
                    }
                scenario == "content" -> listOf(MovieUi(1, "The Long Journey", null, "2024", "Drama", 8.5))
                else ->
                    listOf(
                        MovieUi(1, "The Long Journey", null, "2024", "Drama", 8.5),
                        MovieUi(2, "Arrival", null, "2016", "Science Fiction", 7.9),
                    )
            }
        val state =
            when {
                scenario.startsWith("content") ->
                    WatchlistMoviesUiState(
                        screenState = WatchlistScreenState.Content,
                        movies = movies,
                        totalMoviesCount = movies.size,
                        hasMovies = true,
                        listHeader = WatchlistListHeaderUi.MoviesToWatch(movies.size),
                    )
                scenario == "loading" -> WatchlistMoviesUiState()
                scenario == "empty" ->
                    WatchlistMoviesUiState(
                        screenState = WatchlistScreenState.Empty,
                        hasMovies = false,
                    )
                scenario == "noResults" ->
                    WatchlistMoviesUiState(screenState = WatchlistScreenState.NoResults, query = "Unknown")
                scenario == "error" -> WatchlistMoviesUiState(screenState = WatchlistScreenState.Error("Unavailable"))
                else -> error("Unknown scenario")
            }
        paparazzi.snapshot {
            SantoroTheme(darkTheme = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(
                        LocalDensity provides
                            Density(
                                density = LocalDensity.current.density,
                                fontScale = if (scenario.startsWith("contentSpanishLarge")) LARGE_FONT_SCALE else 1f,
                            ),
                    ) {
                        if (scenario.endsWith("WithNavigation")) {
                            WatchlistWithNavigation(state)
                        } else {
                            WatchlistMoviesScreen(state, {}, {})
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WatchlistWithNavigation(state: WatchlistMoviesUiState) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    items =
                        listOf(
                            BottomNavItem(AppIcons.Home, AppIcons.HomeOutlined, SR.string.search_movies, false, {}),
                            BottomNavItem(
                                AppIcons.Watched,
                                AppIcons.WatchedOutlined,
                                SR.string.watched_movies,
                                false,
                                {},
                            ),
                            BottomNavItem(
                                AppIcons.Watchlist,
                                AppIcons.WatchlistOutlined,
                                SR.string.watchlist,
                                true,
                                {},
                            ),
                            BottomNavItem(
                                AppIcons.Profile,
                                AppIcons.ProfileOutlined,
                                SR.string.profile_title,
                                false,
                                {},
                            ),
                        ),
                )
            },
        ) { padding ->
            WatchlistMoviesScreen(state, {}, {}, modifier = Modifier.padding(padding))
        }
    }

    companion object {
        private const val LARGE_FONT_SCALE = 2f

        @JvmStatic
        @Parameterized.Parameters(name = "{0}-dark={1}")
        fun scenarios(): List<Array<Any>> =
            listOf(
                "content",
                "contentMultiple",
                "contentSpanishLarge",
                "contentWithNavigation",
                "contentSpanishLargeWithNavigation",
                "contentLongWithNavigation",
                "contentSpanishLargeLongWithNavigation",
                "loading",
                "empty",
                "noResults",
                "error",
            ).flatMap { scenario ->
                listOf(arrayOf(scenario, false), arrayOf(scenario, true))
            }
    }
}
