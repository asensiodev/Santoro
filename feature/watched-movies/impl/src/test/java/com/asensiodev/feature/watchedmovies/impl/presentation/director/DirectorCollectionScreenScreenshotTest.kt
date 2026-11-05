package com.asensiodev.feature.watchedmovies.impl.presentation.director

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

@RunWith(Parameterized::class)
class DirectorCollectionScreenScreenshotTest(
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
                "partial", SPANISH_LARGE -> partialState
                "pendingHidden" -> partialState.copy(showPending = false)
                "shorts" -> shortsState
                "loading" -> localState(FilmographyState.LOADING)
                "error" -> localState(FilmographyState.ERROR)
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
                        DirectorCollectionScreen(state, {}, {}, {}, {})
                    }
                }
            }
        }
    }

    companion object {
        private const val SPANISH_LARGE = "spanishLarge"
        private const val LARGE_FONT_SCALE = 2f
        private const val SHORTS_SCENARIO_FEATURES = 3
        private const val NAME = "Christopher Nolan"
        private val films =
            listOf(
                DirectorFilm(11660, "Following", null, "1998-09-12", isWatched = false),
                DirectorFilm(77, "Memento", null, "2000-09-05", isWatched = true),
                DirectorFilm(320, "Insomnia", null, "2002-05-24", isWatched = false),
                DirectorFilm(272, "Batman Begins", null, "2005-06-10", isWatched = true),
                DirectorFilm(1124, "The Prestige", null, "2006-10-19", isWatched = true),
                DirectorFilm(155, "The Dark Knight", null, "2008-07-16", isWatched = true),
                DirectorFilm(27205, "Inception", null, "2010-07-15", isWatched = false),
                DirectorFilm(49026, "The Dark Knight Rises", null, "2012-07-16", isWatched = false),
                DirectorFilm(157336, "Interstellar", null, "2014-11-05", isWatched = true),
            )
        private val partialState =
            DirectorCollectionUiState(
                directorName = NAME,
                films = films,
                watchedCount = films.count { it.isWatched },
                totalCount = films.size,
                filmographyState = FilmographyState.CONTENT,
            )

        private val shortsState =
            films.take(SHORTS_SCENARIO_FEATURES).let { features ->
                partialState.copy(
                    films = features,
                    shorts =
                        listOf(
                            DirectorFilm(1000, "Tarantella", null, "1989-01-01", isWatched = false),
                            DirectorFilm(1001, "Larceny", null, "1996-01-01", isWatched = true),
                            DirectorFilm(1002, "Doodlebug", null, "1997-01-01", isWatched = false),
                        ),
                    watchedCount = features.count { it.isWatched },
                    totalCount = features.size,
                )
            }

        private fun localState(filmographyState: FilmographyState): DirectorCollectionUiState {
            val watched = films.filter { it.isWatched }
            return DirectorCollectionUiState(
                directorName = NAME,
                films = watched,
                watchedCount = watched.size,
                totalCount = null,
                filmographyState = filmographyState,
            )
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}-dark={1}")
        fun scenarios(): List<Array<Any>> =
            listOf("partial", "pendingHidden", "shorts", "loading", "error", SPANISH_LARGE).flatMap { scenario ->
                listOf(arrayOf(scenario, false), arrayOf(scenario, true))
            }
    }
}
