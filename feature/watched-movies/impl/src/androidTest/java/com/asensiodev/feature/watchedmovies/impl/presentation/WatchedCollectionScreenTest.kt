package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.feature.watchedmovies.impl.presentation.component.WatchedGroupingChips
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupLabel
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupUi
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.asensiodev.santoro.core.stringresources.R as SR

@RunWith(AndroidJUnit4::class)
class WatchedCollectionScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val intents = mutableListOf<WatchedMoviesIntent>()
    private var clickedMovieId: Int? = null
    private var clickedDirectorId: Int? = null

    @Test
    fun givenShelfMode_whenListIsSelected_thenRequestsListMode() {
        setScreen(contentState())

        composeRule
            .onNodeWithContentDescription(
                string(SR.string.watched_view_mode_shelf),
            ).assertIsSelected()
        composeRule
            .onNodeWithContentDescription(
                string(SR.string.watched_view_mode_list),
            ).performClick()

        composeRule.runOnIdle {
            intents shouldBeEqualTo listOf(WatchedMoviesIntent.ChangeViewMode(WatchedViewMode.LIST))
        }
    }

    @Test
    fun givenDateGrouping_whenDirectorChipIsSelected_thenRequestsDirectorGrouping() {
        setScreen(contentState())

        composeRule.onNodeWithText(string(SR.string.watched_group_date)).assertIsSelected()
        composeRule.onNodeWithText(string(SR.string.watched_group_director)).performClick()

        composeRule.runOnIdle {
            intents shouldBeEqualTo
                listOf(WatchedMoviesIntent.ChangeGrouping(WatchedGrouping.DIRECTOR))
        }
    }

    @Test
    fun givenListMode_whenRowIsShown_thenShowsYearAndDirectorsAndOpensMovie() {
        setScreen(contentState(viewMode = WatchedViewMode.LIST))

        composeRule.onNodeWithText("1996 · Joel Coen, Ethan Coen").assertIsDisplayed()
        composeRule.onNodeWithText("Fargo").performClick()

        composeRule.runOnIdle { clickedMovieId shouldBeEqualTo FARGO_ID }
    }

    @Test
    fun givenDirectorGroup_whenHeadingIsClicked_thenOpensDirectorAndShowsCount() {
        setScreen(contentState(grouping = WatchedGrouping.DIRECTOR))

        composeRule
            .onNodeWithText(
                pluralString(SR.plurals.watched_group_movie_count, 1),
            ).assertIsDisplayed()
        composeRule.onNodeWithText("Joel Coen").performClick()

        composeRule.runOnIdle { clickedDirectorId shouldBeEqualTo JOEL_COEN_ID }
    }

    @Test
    fun givenUnknownDirectorGroup_whenShown_thenHeadingIsNotActionable() {
        setScreen(
            contentState(
                grouping = WatchedGrouping.DIRECTOR,
                groups =
                    listOf(
                        WatchedGroupUi(
                            "director:unknown",
                            WatchedGroupLabel.UnknownDirector,
                            listOf(fargo),
                        ),
                    ),
            ),
        )

        composeRule
            .onNodeWithText(string(SR.string.watched_group_unknown_director))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
    }

    @Test
    fun givenEmptyCollection_whenShown_thenCollectionControlsAreHidden() {
        setScreen(WatchedMoviesUiState(screenState = WatchedScreenState.Empty, hasMovies = false))

        composeRule.onNodeWithText(string(SR.string.watched_movies)).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.watched_group_director)).assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(
                string(SR.string.watched_view_mode_list),
            ).assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(
                string(SR.string.watched_movies_textfield_placeholder),
            ).assertDoesNotExist()
    }

    @Test
    fun givenLoading_whenShown_thenHeaderShowsOnlyTitle() {
        setScreen(WatchedMoviesUiState())

        composeRule.onNodeWithText(string(SR.string.watched_movies)).assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(
                string(SR.string.watched_movies_textfield_placeholder),
            ).assertDoesNotExist()
        composeRule.onNode(hasSetTextAction()).assertDoesNotExist()
    }

    @Test
    fun givenPopulatedCollection_whenShown_thenHeaderShowsTitleAndTotalWithoutStats() {
        setScreen(contentState().copy(totalMovies = TOTAL_MOVIES))

        composeRule.onNodeWithText(string(SR.string.watched_movies)).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                pluralString(SR.plurals.watched_group_movie_count, TOTAL_MOVIES),
            ).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.watched_stat_total_label)).assertDoesNotExist()
    }

    @Test
    fun givenClosedSearch_whenSearchButtonIsClicked_thenRequestsOpenSearch() {
        setScreen(contentState())

        composeRule
            .onNodeWithContentDescription(
                string(SR.string.watched_movies_textfield_placeholder),
            ).performClick()

        composeRule.runOnIdle { intents shouldBeEqualTo listOf(WatchedMoviesIntent.OpenSearch) }
    }

    @Test
    fun givenOpenSearch_whenShown_thenFieldIsFocusedToggleIsHiddenAndChipsRemain() {
        setScreen(contentState().copy(isSearchOpen = true))

        composeRule.onNode(hasSetTextAction()).assertIsFocused()
        composeRule
            .onNodeWithContentDescription(
                string(SR.string.watched_view_mode_list),
            ).assertDoesNotExist()
        composeRule.onNodeWithText(string(SR.string.watched_movies)).assertDoesNotExist()
        composeRule.onNodeWithText(string(SR.string.watched_group_director)).assertIsDisplayed()
    }

    @Test
    fun givenOpenSearch_whenCloseActionIsClicked_thenRequestsCloseSearch() {
        setScreen(contentState().copy(isSearchOpen = true, query = "dune"))

        composeRule
            .onNodeWithContentDescription(
                string(SR.string.watched_search_close),
            ).performClick()

        composeRule.runOnIdle { intents shouldBeEqualTo listOf(WatchedMoviesIntent.CloseSearch) }
    }

    @Test
    fun givenOpenSearch_whenSystemBackIsPressed_thenRequestsCloseSearch() {
        setScreen(contentState().copy(isSearchOpen = true, query = "dune"))

        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }

        composeRule.runOnIdle { intents shouldBeEqualTo listOf(WatchedMoviesIntent.CloseSearch) }
    }

    @Test
    fun givenOpenSearchWithQuery_whenClearIsClicked_thenClearsQueryWithoutClosingSearch() {
        setScreen(contentState().copy(isSearchOpen = true, query = "dune"))

        composeRule
            .onNodeWithContentDescription(
                string(SR.string.query_text_field_clear_button_description),
            ).performClick()

        composeRule.runOnIdle {
            intents shouldBeEqualTo
                listOf(
                    WatchedMoviesIntent.UpdateQuery(""),
                )
        }
    }

    @Test
    fun givenScrolledSearchResults_whenSearchCloses_thenGridReturnsToTheTop() {
        val longGroups =
            List(LONG_GROUP_COUNT) { index ->
                WatchedGroupUi(
                    key = "decade:$index",
                    label = WatchedGroupLabel.Text("Group $index"),
                    movies =
                        List(
                            MOVIES_PER_GROUP,
                        ) { movie ->
                            fargo.copy(
                                id = index * 10 + movie,
                                title = "Movie $index-$movie",
                            )
                        },
                )
            }
        val state =
            mutableStateOf(
                contentState(
                    viewMode = WatchedViewMode.LIST,
                    groups = longGroups,
                ).copy(isSearchOpen = true, query = "movie"),
            )
        composeRule.setContent {
            SantoroTheme {
                WatchedMoviesScreen(
                    uiState = state.value,
                    onProcess = {},
                    onMovieClick = {},
                    onDirectorClick = {},
                )
            }
        }
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(
            LONG_GROUP_COUNT * (MOVIES_PER_GROUP + 1) - 1,
        )
        composeRule.onNodeWithText("Group 0").assertDoesNotExist()

        composeRule.runOnIdle { state.value = state.value.copy(isSearchOpen = false, query = "") }

        composeRule.onNodeWithText("Group 0").assertIsDisplayed()
    }

    @Test
    fun givenDecadeGroupingOnNarrowWidth_whenShown_thenSelectedChipIsVisible() {
        composeRule.setContent {
            SantoroTheme {
                Box(modifier = Modifier.width(NARROW_WIDTH)) {
                    WatchedGroupingChips(
                        grouping = WatchedGrouping.DECADE,
                        onGroupingChange = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText(string(SR.string.watched_group_decade)).assertIsDisplayed()
    }

    private fun setScreen(state: WatchedMoviesUiState) {
        composeRule.setContent {
            SantoroTheme {
                WatchedMoviesScreen(
                    uiState = state,
                    onProcess = { intents += it },
                    onMovieClick = { clickedMovieId = it },
                    onDirectorClick = { clickedDirectorId = it },
                )
            }
        }
    }

    private fun contentState(
        viewMode: WatchedViewMode = WatchedViewMode.SHELF,
        grouping: WatchedGrouping = WatchedGrouping.DATE_WATCHED,
        groups: List<WatchedGroupUi> =
            listOf(
                WatchedGroupUi(
                    key = "director:$JOEL_COEN_ID",
                    label = WatchedGroupLabel.Text("Joel Coen"),
                    movies = listOf(fargo),
                    directorId = JOEL_COEN_ID,
                ),
            ),
    ) = WatchedMoviesUiState(
        screenState = WatchedScreenState.Content,
        groups = groups,
        viewMode = viewMode,
        grouping = grouping,
        hasMovies = true,
    )

    private fun string(resId: Int) = composeRule.activity.getString(resId)

    private fun pluralString(
        resId: Int,
        count: Int,
    ) = composeRule.activity.resources.getQuantityString(resId, count, count)

    private companion object {
        const val FARGO_ID = 275
        const val JOEL_COEN_ID = 1223
        const val TOTAL_MOVIES = 48
        const val LONG_GROUP_COUNT = 8
        const val MOVIES_PER_GROUP = 3
        val NARROW_WIDTH = 280.dp
        val fargo =
            MovieUi(
                id = FARGO_ID,
                title = "Fargo",
                posterPath = null,
                watchedDate = "March 2026",
                releaseYear = "1996",
                directors = "Joel Coen, Ethan Coen",
            )
    }
}
