package com.asensiodev.feature.watchlist.impl.presentation

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.Density
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.watchlist.impl.presentation.component.WatchlistSurpriseContent
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import com.asensiodev.santoro.core.stringresources.R as SR

@RunWith(AndroidJUnit4::class)
class WatchlistSurpriseScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val arrival = MovieUi(1, "Arrival", null, "2016", "Science Fiction", 7.9)
    private val content =
        WatchlistMoviesUiState(
            screenState = WatchlistScreenState.Content,
            movies = listOf(arrival),
            listHeader = WatchlistListHeaderUi.MoviesToWatch(1),
        )

    @Test
    fun surpriseActionRemainsReachableAfterScrollingToTheLastMovie() {
        val movies = (1..20).map { arrival.copy(id = it, title = "Saved movie $it") }
        var intent: WatchlistIntent? = null
        var fontScale by mutableStateOf(1f)
        val configuration =
            Configuration(composeRule.activity.resources.configuration).apply {
                setLocale(Locale.forLanguageTag("es"))
            }
        val context = composeRule.activity.createConfigurationContext(configuration)
        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides context,
                LocalConfiguration provides configuration,
                LocalDensity provides Density(LocalDensity.current.density, fontScale),
            ) {
                SantoroTheme {
                    WatchlistMoviesScreen(
                        content.copy(
                            movies = movies,
                            listHeader = WatchlistListHeaderUi.MoviesToWatch(movies.size),
                        ),
                        { intent = it },
                        {},
                    )
                }
            }
        }

        listOf(1f, 2f).forEach { scale ->
            composeRule.runOnIdle {
                fontScale = scale
                intent = null
            }
            val action =
                composeRule.onNodeWithText(
                    context.getString(SR.string.watchlist_surprise_action),
                )
            val list = composeRule.onNode(hasScrollAction())
            val viewport = list.fetchSemanticsNode().boundsInRoot
            val actionTop = action.fetchSemanticsNode().boundsInRoot.top
            (viewport.bottom > actionTop) shouldBeEqualTo true

            list.performScrollToIndex(movies.size)
            val lastMovie =
                composeRule
                    .onNodeWithText(
                        "Saved movie 20",
                    ).assertIsDisplayed()
                    .fetchSemanticsNode()
            action.assertIsDisplayed().performClick()

            intent shouldBeEqualTo WatchlistIntent.SuggestMovie
            val actionBounds = action.fetchSemanticsNode().boundsInRoot
            (lastMovie.boundsInRoot.bottom <= actionBounds.top) shouldBeEqualTo true
        }
    }

    @Test
    fun singleMovieWatchlistDoesNotOfferSurpriseAction() {
        composeRule.setContent {
            SantoroTheme {
                WatchlistMoviesScreen(content, {}, {})
            }
        }

        composeRule
            .onNodeWithText(
                composeRule.activity.getString(SR.string.watchlist_surprise_action),
            ).assertDoesNotExist()
        composeRule.onNodeWithText("Arrival").assertIsDisplayed()
    }

    @Test
    fun populatedWatchlistOffersSurpriseAction() {
        var intent: WatchlistIntent? = null
        composeRule.setContent {
            SantoroTheme {
                WatchlistMoviesScreen(
                    content.copy(
                        movies = listOf(arrival, arrival.copy(id = 2, title = "Inception")),
                        listHeader = WatchlistListHeaderUi.MoviesToWatch(2),
                    ),
                    { intent = it },
                    {},
                )
            }
        }

        composeRule
            .onNodeWithText(
                composeRule.activity.getString(SR.string.watchlist_surprise_action),
            ).performClick()

        intent shouldBeEqualTo WatchlistIntent.SuggestMovie
    }

    @Test
    fun filteredWatchlistDoesNotOfferSurpriseAction() {
        composeRule.setContent {
            SantoroTheme {
                WatchlistMoviesScreen(
                    content.copy(
                        query = "Arrival",
                        movies = listOf(arrival, arrival.copy(id = 2, title = "Inception")),
                        listHeader = WatchlistListHeaderUi.SearchResults(2),
                    ),
                    {},
                    {},
                )
            }
        }

        composeRule
            .onNodeWithText(
                composeRule.activity.getString(SR.string.watchlist_surprise_action),
            ).assertDoesNotExist()
    }

    @Test
    fun suggestedMovieOpensItsDetailAfterDismissingThePanel() {
        val actions = mutableListOf<String>()
        composeRule.setContent {
            SantoroTheme {
                WatchlistMoviesScreen(
                    content.copy(suggestedMovie = arrival),
                    { if (it == WatchlistIntent.DismissSuggestion) actions.add("dismiss") },
                    { actions.add("detail:$it") },
                )
            }
        }

        composeRule
            .onNodeWithText(
                composeRule.activity.getString(SR.string.watchlist_surprise_title),
            ).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                composeRule.activity.getString(SR.string.watchlist_surprise_details),
            ).performClick()

        actions shouldBeEqualTo listOf("dismiss", "detail:1")
    }

    @Test
    fun anotherActionRequestsANewSuggestionAndRendersTheNewMovie() {
        val inception = arrival.copy(id = 2, title = "Inception")
        var selected by mutableStateOf(arrival)
        var intent: WatchlistIntent? = null
        composeRule.setContent {
            SantoroTheme {
                WatchlistMoviesScreen(
                    content.copy(movies = listOf(arrival, inception), suggestedMovie = selected),
                    {
                        intent = it
                        if (it == WatchlistIntent.SuggestMovie) selected = inception
                    },
                    {},
                )
            }
        }

        composeRule
            .onNodeWithText(
                composeRule.activity.getString(SR.string.watchlist_surprise_another),
            ).performClick()

        intent shouldBeEqualTo WatchlistIntent.SuggestMovie
        composeRule.onNode(hasText("Inception") and hasAnyAncestor(isDialog())).assertIsDisplayed()
    }

    @Test
    fun singleMovieSuggestionOmitsAnotherAction() {
        composeRule.setContent {
            SantoroTheme {
                WatchlistMoviesScreen(content.copy(suggestedMovie = arrival), {}, {})
            }
        }

        composeRule
            .onNodeWithText(
                composeRule.activity.getString(SR.string.watchlist_surprise_details),
            ).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                composeRule.activity.getString(SR.string.watchlist_surprise_another),
            ).assertDoesNotExist()
    }

    @Test
    fun backDismissesTheSuggestion() {
        var intent: WatchlistIntent? = null
        composeRule.setContent {
            SantoroTheme {
                WatchlistMoviesScreen(content.copy(suggestedMovie = arrival), { intent = it }, {})
            }
        }
        composeRule
            .onNodeWithText(
                composeRule.activity.getString(SR.string.watchlist_surprise_details),
            ).assertIsDisplayed()

        pressBack()

        composeRule.runOnIdle {
            intent shouldBeEqualTo WatchlistIntent.DismissSuggestion
        }
    }

    @Test
    fun unavailableContentDoesNotOfferSurpriseAction() {
        var state by mutableStateOf(WatchlistMoviesUiState())
        composeRule.setContent {
            SantoroTheme {
                WatchlistMoviesScreen(state, {}, {})
            }
        }
        composeRule
            .onNodeWithText(
                composeRule.activity.getString(SR.string.watchlist_surprise_action),
            ).assertDoesNotExist()

        listOf(
            WatchlistScreenState.Empty,
            WatchlistScreenState.NoResults,
            WatchlistScreenState.Error("Unavailable"),
        ).forEach { screenState ->
            composeRule.runOnIdle { state = content.copy(screenState = screenState) }
            composeRule
                .onNodeWithText(
                    composeRule.activity.getString(SR.string.watchlist_surprise_action),
                ).assertDoesNotExist()
        }
    }

    @Test
    fun suggestionActionsAreLocalizedInSpanish() {
        val configuration =
            Configuration(composeRule.activity.resources.configuration).apply {
                setLocale(Locale.forLanguageTag("es"))
            }
        val context = composeRule.activity.createConfigurationContext(configuration)
        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides context,
                LocalConfiguration provides configuration,
            ) {
                SantoroTheme {
                    WatchlistSurpriseContent(arrival, true, {}, {})
                }
            }
        }

        composeRule.onNodeWithText("¿Qué vemos hoy?").assertIsDisplayed()
        composeRule.onNodeWithText("Ver ficha").assertIsDisplayed()
        composeRule.onNodeWithText("Elegir otra").assertIsDisplayed()
    }
}
