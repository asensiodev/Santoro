package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.asensiodev.santoro.core.stringresources.R as SR

@RunWith(AndroidJUnit4::class)
class MovieDetailWithoutRemoteDataTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun givenMovieWithoutCast_whenShown_thenCastHeadingIsHidden() {
        composeRule.setContent {
            SantoroTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MovieDetailContent(
                        uiState =
                            MovieDetailUiState(
                                screenState = MovieDetailScreenState.Content,
                                movie = restoredMovie,
                            ),
                        onToggleWatchlist = {},
                        onToggleWatched = {},
                        onDismissTooltip = {},
                        scrollState = rememberScrollState(),
                    )
                }
            }
        }

        composeRule
            .onNodeWithText(composeRule.activity.getString(SR.string.movie_detail_section_overview))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(
                composeRule.activity.getString(SR.string.cast_title),
            ).assertDoesNotExist()
    }

    private val restoredMovie =
        MovieUi(
            id = 460465,
            title = "Mortal Kombat",
            overview = "",
            posterPath = null,
            releaseDate = "2021-04-07",
            popularity = 0.0,
            voteAverage = 0.0,
            voteCount = 0,
            genres = emptyList(),
            productionCountries = emptyList(),
            cast = emptyList(),
            runtime = "1h 50m",
            director = null,
            isWatched = true,
            isInWatchlist = false,
        )
}
