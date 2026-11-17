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
            deviceConfig = DeviceConfig.PIXEL_3.copy(locale = if (scenario.startsWith(SPANISH_LARGE)) "es" else "en"),
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
                                    key = "recent",
                                    label = null,
                                    movies = listOf(MovieUi(1, "The Long Journey", null)),
                                ),
                            ),
                        hasMovies = true,
                        totalMovies = 1,
                    )
                "shelfDirector" ->
                    collectionState(WatchedViewMode.SHELF, WatchedGrouping.DIRECTOR, partlyExpandedDirectorGroups)
                "listDirector" -> collectionState(WatchedViewMode.LIST, WatchedGrouping.DIRECTOR, directorGroups)
                "listRecent" -> collectionState(WatchedViewMode.LIST, WatchedGrouping.RECENT, recentGroups)
                "listAlphabetical" ->
                    collectionState(WatchedViewMode.LIST, WatchedGrouping.ALPHABETICAL, alphabeticalGroups)
                "listDecade" -> collectionState(WatchedViewMode.LIST, WatchedGrouping.DECADE, decadeGroups)
                SPANISH_LARGE_DECADE ->
                    collectionState(WatchedViewMode.SHELF, WatchedGrouping.DECADE, decadeGroups)
                SPANISH_LARGE_DIRECTOR ->
                    collectionState(WatchedViewMode.LIST, WatchedGrouping.DIRECTOR, partlyExpandedDirectorGroups)
                "searchOpen" ->
                    collectionState(WatchedViewMode.SHELF, WatchedGrouping.GENRE, genreSearchGroups)
                        .copy(isSearchOpen = true, query = "dune")
                "loading" -> WatchedMoviesUiState()
                "empty" -> WatchedMoviesUiState(screenState = WatchedScreenState.Empty, hasMovies = false)
                "noResults" ->
                    collectionState(WatchedViewMode.SHELF, WatchedGrouping.RECENT, emptyList())
                        .copy(screenState = WatchedScreenState.NoResults, isSearchOpen = true, query = "Unknown")
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
                                fontScale = if (scenario.startsWith(SPANISH_LARGE)) LARGE_FONT_SCALE else 1f,
                            ),
                    ) {
                        WatchedMoviesScreen(state, {}, {}, { _, _ -> })
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
        totalMovies = TOTAL_MOVIES,
    )

    companion object {
        private const val SPANISH_LARGE = "spanishLarge"
        private const val SPANISH_LARGE_DECADE = "spanishLargeDecade"
        private const val SPANISH_LARGE_DIRECTOR = "spanishLargeDirector"
        private const val LARGE_FONT_SCALE = 2f
        private val fargo = MovieUi(275, "Fargo", null, "1996", "Joel Coen, Ethan Coen")
        private val noCountry =
            MovieUi(6977, "No Country for Old Men", null, "2007", "Joel Coen, Ethan Coen")
        private val dune = MovieUi(438631, "Dune", null, "2021", "Denis Villeneuve")
        private val arrival = MovieUi(329865, "Arrival", null, "2016", "Denis Villeneuve")
        private val sicario = MovieUi(273481, "Sicario", null, "2015", "Denis Villeneuve")
        private val unknown = MovieUi(1, "The Long Journey", null, "2024", null)
        private val twelveAngryMen = MovieUi(389, "12 Angry Men", null, "1957", "Sidney Lumet")
        private val directorGroups =
            listOf(
                WatchedGroupUi(
                    "director:137427",
                    WatchedGroupLabel.Text("Denis Villeneuve"),
                    listOf(sicario, arrival, dune),
                    137427,
                    isCollapsible = true,
                    isExpanded = false,
                ),
                WatchedGroupUi(
                    "director:1223",
                    WatchedGroupLabel.Text("Joel Coen"),
                    listOf(fargo, noCountry),
                    1223,
                    isCollapsible = true,
                    isExpanded = false,
                ),
                WatchedGroupUi(
                    "director:unknown",
                    WatchedGroupLabel.UnknownDirector,
                    listOf(unknown),
                    isCollapsible = true,
                    isExpanded = false,
                ),
            )
        private val partlyExpandedDirectorGroups =
            directorGroups.mapIndexed { index, group -> group.copy(isExpanded = index == 0) }
        private const val TOTAL_MOVIES = 6
        private val recentGroups =
            listOf(WatchedGroupUi("recent", null, listOf(fargo, noCountry, unknown, dune)))
        private val alphabeticalGroups =
            listOf(
                WatchedGroupUi("letter:A", WatchedGroupLabel.Text("A"), listOf(arrival)),
                WatchedGroupUi("letter:D", WatchedGroupLabel.Text("D"), listOf(dune)),
                WatchedGroupUi("letter:F", WatchedGroupLabel.Text("F"), listOf(fargo)),
                WatchedGroupUi("letter:other", WatchedGroupLabel.OtherTitles, listOf(twelveAngryMen)),
            )
        private val genreSearchGroups =
            listOf(
                WatchedGroupUi("genre:878", WatchedGroupLabel.Text("Science Fiction"), listOf(dune)),
                WatchedGroupUi("genre:12", WatchedGroupLabel.Text("Adventure"), listOf(dune)),
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
                "listRecent",
                "listAlphabetical",
                "listDecade",
                SPANISH_LARGE_DECADE,
                SPANISH_LARGE_DIRECTOR,
                "searchOpen",
                "loading",
                "empty",
                "noResults",
                "error",
            ).flatMap { scenario ->
                listOf(arrayOf(scenario, false), arrayOf(scenario, true))
            }
    }
}
