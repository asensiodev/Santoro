package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupLabel
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupUi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

@RunWith(Parameterized::class)
class WatchedMoviesScreenScreenshotTest(
    private val scenario: String,
    private val dark: Boolean,
) {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_3.copy(locale = if (scenario == SPANISH_LARGE) "es" else "en"),
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
        val state =
            when (scenario) {
                "content" ->
                    WatchedMoviesUiState(
                        screenState = WatchedScreenState.Content,
                        groups =
                            listOf(
                                WatchedGroupUi(
                                    key = "date:2024",
                                    label = WatchedGroupLabel.Text("2024"),
                                    movies = listOf(MovieUi(1, "The Long Journey", null, "Mar 15")),
                                ),
                            ),
                        hasMovies = true,
                    )
                "shelfDirector" -> collectionState(WatchedViewMode.SHELF, WatchedGrouping.DIRECTOR, directorGroups)
                "listDirector" -> collectionState(WatchedViewMode.LIST, WatchedGrouping.DIRECTOR, directorGroups)
                "listDecade" -> collectionState(WatchedViewMode.LIST, WatchedGrouping.DECADE, decadeGroups)
                SPANISH_LARGE -> collectionState(WatchedViewMode.SHELF, WatchedGrouping.DECADE, decadeGroups)
                "loading" -> WatchedMoviesUiState()
                "empty" -> WatchedMoviesUiState(screenState = WatchedScreenState.Empty, hasMovies = false)
                "noResults" -> WatchedMoviesUiState(screenState = WatchedScreenState.NoResults, query = "Unknown")
                "error" -> WatchedMoviesUiState(screenState = WatchedScreenState.Error("Unavailable"))
                else -> error("Unknown scenario")
            }
        paparazzi.snapshot {
            SantoroTheme(darkTheme = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(
                        LocalDensity provides
                            Density(
                                density = LocalDensity.current.density,
                                fontScale = if (scenario == SPANISH_LARGE) LARGE_FONT_SCALE else 1f,
                            ),
                    ) {
                        WatchedMoviesScreen(state, {}, {}, {})
                    }
                }
            }
        }
    }

    private fun collectionState(
        viewMode: WatchedViewMode,
        grouping: WatchedGrouping,
        groups: List<WatchedGroupUi>,
    ) = WatchedMoviesUiState(
        screenState = WatchedScreenState.Content,
        groups = groups,
        viewMode = viewMode,
        grouping = grouping,
        hasMovies = true,
    )

    companion object {
        private const val SPANISH_LARGE = "spanishLargeDecade"
        private const val LARGE_FONT_SCALE = 2f
        private val fargo = MovieUi(275, "Fargo", null, "March 2026", "1996", "Joel Coen, Ethan Coen")
        private val noCountry =
            MovieUi(6977, "No Country for Old Men", null, "March 2026", "2007", "Joel Coen, Ethan Coen")
        private val dune = MovieUi(438631, "Dune", null, "February 2026", "2021", "Denis Villeneuve")
        private val arrival = MovieUi(329865, "Arrival", null, "January 2026", "2016", "Denis Villeneuve")
        private val sicario = MovieUi(273481, "Sicario", null, "January 2026", "2015", "Denis Villeneuve")
        private val unknown = MovieUi(1, "The Long Journey", null, "March 2026", "2024", null)
        private val directorGroups =
            listOf(
                WatchedGroupUi(
                    "director:137427",
                    WatchedGroupLabel.Text("Denis Villeneuve"),
                    listOf(sicario, arrival, dune),
                    137427,
                ),
                WatchedGroupUi("director:1223", WatchedGroupLabel.Text("Joel Coen"), listOf(fargo, noCountry), 1223),
                WatchedGroupUi("director:unknown", WatchedGroupLabel.UnknownDirector, listOf(unknown)),
            )
        private val decadeGroups =
            listOf(
                WatchedGroupUi("decade:2020", WatchedGroupLabel.Decade(2020), listOf(dune, unknown)),
                WatchedGroupUi("decade:2010", WatchedGroupLabel.Decade(2010), listOf(sicario, arrival)),
                WatchedGroupUi("decade:2000", WatchedGroupLabel.Decade(2000), listOf(noCountry)),
                WatchedGroupUi("decade:1990", WatchedGroupLabel.Decade(1990), listOf(fargo)),
            )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}-dark={1}")
        fun scenarios(): List<Array<Any>> =
            listOf(
                "content",
                "shelfDirector",
                "listDirector",
                "listDecade",
                SPANISH_LARGE,
                "loading",
                "empty",
                "noResults",
                "error",
            ).flatMap { scenario ->
                listOf(arrayOf(scenario, false), arrayOf(scenario, true))
            }
    }
}
