package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

        composeRule.onNodeWithText(string(SR.string.watched_group_director)).assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(
                string(SR.string.watched_view_mode_list),
            ).assertDoesNotExist()
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
