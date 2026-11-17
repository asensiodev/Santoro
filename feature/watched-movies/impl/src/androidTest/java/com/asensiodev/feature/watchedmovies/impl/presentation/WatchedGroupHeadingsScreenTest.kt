package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupLabel
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupUi
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.asensiodev.santoro.core.stringresources.R as SR

@RunWith(AndroidJUnit4::class)
class WatchedGroupHeadingsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val intents = mutableListOf<WatchedMoviesIntent>()
    private var clickedDirectorId: Int? = null

    @Test
    fun givenRecentInShelf_whenShown_thenShowsMoviesWithoutHeading() {
        setScreen(recentState(WatchedViewMode.SHELF))

        composeRule.onNodeWithText("Fargo").assertIsDisplayed()
        composeRule
            .onNodeWithText(pluralString(SR.plurals.watched_group_movie_count, 2))
            .assertDoesNotExist()
    }

    @Test
    fun givenRecentInList_whenShown_thenShowsRowsWithoutHeading() {
        setScreen(recentState(WatchedViewMode.LIST))

        composeRule.onNodeWithText("Fargo").assertIsDisplayed()
        composeRule.onNodeWithText("Arrival").assertIsDisplayed()
        composeRule
            .onNodeWithText(pluralString(SR.plurals.watched_group_movie_count, 2))
            .assertDoesNotExist()
    }

    @Test
    fun givenAlphabeticalGrouping_whenShown_thenShowsLetterAndOtherTitlesHeadings() {
        setScreen(
            contentState(
                grouping = WatchedGrouping.ALPHABETICAL,
                viewMode = WatchedViewMode.LIST,
                groups =
                    listOf(
                        WatchedGroupUi("letter:F", WatchedGroupLabel.Text("F"), listOf(fargo)),
                        WatchedGroupUi(
                            "letter:other",
                            WatchedGroupLabel.OtherTitles,
                            listOf(twelveAngryMen),
                        ),
                    ),
            ),
        )

        composeRule.onNodeWithText("F").assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(string(SR.string.watched_group_other_titles_description))
            .assertIsDisplayed()
    }

    @Test
    fun givenGroupingChips_whenShown_thenOrderIsRecentAlphabeticalDirectorGenreDecade() {
        setScreen(recentState(WatchedViewMode.SHELF))

        val labels =
            listOf(
                SR.string.watched_group_recent,
                SR.string.watched_group_alphabetical,
                SR.string.watched_group_director,
                SR.string.watched_group_genre,
                SR.string.watched_group_decade,
            ).map { string(it) }
        val lefts = labels.map { composeRule.onNodeWithText(it).getUnclippedBoundsInRoot().left }

        lefts shouldBeEqualTo lefts.sorted()
    }

    @Test
    fun givenCollapsedDirector_whenShown_thenShowsCountWithoutMovies() {
        setScreen(directorState(isExpanded = false))

        composeRule
            .onNodeWithText(
                pluralString(SR.plurals.watched_group_movie_count, 1),
            ).assertIsDisplayed()
        composeRule.onNodeWithText("Fargo").assertDoesNotExist()
    }

    @Test
    fun givenCollapsedDirector_whenExpandIsClicked_thenTogglesWithoutOpeningDirector() {
        setScreen(directorState(isExpanded = false))

        composeRule
            .onNodeWithContentDescription(formatted(SR.string.watched_group_expand, JOEL_COEN))
            .assert(hasStateDescription(string(SR.string.watched_group_collapsed)))
            .assertWidthIsAtLeast(MIN_TOUCH_TARGET)
            .assertHeightIsAtLeast(MIN_TOUCH_TARGET)
            .performClick()

        composeRule.runOnIdle {
            intents shouldBeEqualTo listOf(WatchedMoviesIntent.ToggleGroup(JOEL_COEN_KEY))
            clickedDirectorId shouldBeEqualTo null
        }
    }

    @Test
    fun givenExpandedDirector_whenShown_thenShowsMoviesAndCollapseControl() {
        setScreen(directorState(isExpanded = true))

        composeRule.onNodeWithText("Fargo").assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(formatted(SR.string.watched_group_collapse, JOEL_COEN))
            .assert(hasStateDescription(string(SR.string.watched_group_expanded)))
    }

    @Test
    fun givenCollapsedDirector_whenNameIsClicked_thenOpensDirector() {
        setScreen(directorState(isExpanded = false))

        composeRule.onNodeWithText(JOEL_COEN).performClick()

        composeRule.runOnIdle {
            clickedDirectorId shouldBeEqualTo JOEL_COEN_ID
            intents shouldBeEqualTo emptyList()
        }
    }

    @Test
    fun givenUnknownDirector_whenShown_thenHasExpandControlButNoDirectorButton() {
        val unknownDirector = string(SR.string.watched_group_unknown_director)
        setScreen(
            contentState(
                grouping = WatchedGrouping.DIRECTOR,
                groups =
                    listOf(
                        WatchedGroupUi(
                            key = "director:unknown",
                            label = WatchedGroupLabel.UnknownDirector,
                            movies = listOf(fargo),
                            isCollapsible = true,
                            isExpanded = false,
                        ),
                    ),
            ),
        )

        composeRule
            .onNodeWithContentDescription(
                formatted(SR.string.watched_group_expand, unknownDirector),
            ).assertIsDisplayed()
        composeRule
            .onNodeWithText(unknownDirector)
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
    }

    @Test
    fun givenSearchResults_whenDirectorGroupsAreNotCollapsible_thenExpandControlIsHidden() {
        setScreen(directorState(isExpanded = true, isCollapsible = false, query = "far"))

        composeRule.onNodeWithText("Fargo").assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(formatted(SR.string.watched_group_collapse, JOEL_COEN))
            .assertDoesNotExist()
    }

    @Test
    fun givenGenreGroup_whenShown_thenHasNoExpandControl() {
        setScreen(
            contentState(
                grouping = WatchedGrouping.GENRE,
                groups =
                    listOf(
                        WatchedGroupUi("genre:18", WatchedGroupLabel.Text("Drama"), listOf(fargo)),
                    ),
            ),
        )

        composeRule.onNodeWithText("Fargo").assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(formatted(SR.string.watched_group_collapse, "Drama"))
            .assertDoesNotExist()
    }

    private fun directorState(
        isExpanded: Boolean,
        isCollapsible: Boolean = true,
        query: String = "",
    ) = contentState(
        grouping = WatchedGrouping.DIRECTOR,
        query = query,
        groups =
            listOf(
                WatchedGroupUi(
                    key = JOEL_COEN_KEY,
                    label = WatchedGroupLabel.Text(JOEL_COEN),
                    movies = listOf(fargo),
                    directorId = JOEL_COEN_ID,
                    isCollapsible = isCollapsible,
                    isExpanded = isExpanded,
                ),
            ),
    )

    private fun hasStateDescription(value: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    private fun formatted(
        resId: Int,
        argument: String,
    ) = composeRule.activity.getString(resId, argument)

    private fun recentState(viewMode: WatchedViewMode) =
        contentState(
            viewMode = viewMode,
            groups = listOf(WatchedGroupUi("recent", null, listOf(fargo, arrival))),
        )

    private fun setScreen(state: WatchedMoviesUiState) {
        composeRule.setContent {
            SantoroTheme {
                WatchedMoviesScreen(
                    uiState = state,
                    onProcess = { intents += it },
                    onMovieClick = {},
                    onDirectorClick = { id, _ -> clickedDirectorId = id },
                )
            }
        }
    }

    private fun contentState(
        groups: List<WatchedGroupUi>,
        viewMode: WatchedViewMode = WatchedViewMode.SHELF,
        grouping: WatchedGrouping = WatchedGrouping.RECENT,
        query: String = "",
    ) = WatchedMoviesUiState(
        screenState = WatchedScreenState.Content,
        groups = groups,
        viewMode = viewMode,
        grouping = grouping,
        hasMovies = true,
        query = query,
        isSearchOpen = query.isNotEmpty(),
    )

    private fun string(resId: Int) = composeRule.activity.getString(resId)

    private fun pluralString(
        resId: Int,
        count: Int,
    ) = composeRule.activity.resources.getQuantityString(resId, count, count)

    private companion object {
        const val JOEL_COEN = "Joel Coen"
        const val JOEL_COEN_ID = 1223
        const val JOEL_COEN_KEY = "director:1223"
        val MIN_TOUCH_TARGET = 48.dp
        val fargo = MovieUi(id = 275, title = "Fargo", posterPath = null, releaseYear = "1996")
        val arrival =
            MovieUi(id = 329865, title = "Arrival", posterPath = null, releaseYear = "2016")
        val twelveAngryMen =
            MovieUi(id = 389, title = "12 Angry Men", posterPath = null, releaseYear = "1957")
    }
}
