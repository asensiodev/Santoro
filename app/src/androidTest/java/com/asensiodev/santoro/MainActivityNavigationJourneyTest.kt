package com.asensiodev.santoro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import com.asensiodev.santoro.core.stringresources.R as SR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityNavigationJourneyTest : BaseAppJourneyTest() {
    @Test
    fun givenSearchQuery_whenSwitchingTabs_thenSearchStateIsRestored() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)
        launch()
        val query = "Phase Seven Query"

        composeRule.onNode(hasSetTextAction()).performTextInput(query)
        composeRule.onNodeWithText(string(SR.string.watched_movies)).performClick()
        composeRule.onNodeWithText(AppJourneyTestData.watchedMovie.title).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.search_movies)).performClick()

        composeRule.onNode(hasSetTextAction()).assertTextEquals(query)
    }

    @Test
    fun givenSearchDashboard_whenSeeAllMovieOpened_thenBackRestoresEachSource() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)
        launch()
        val seeAll = string(SR.string.browse_see_all)

        composeRule
            .onAllNodesWithText(seeAll)
            .onFirst()
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText(AppJourneyTestData.trendingMovie.title).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            detailRepository.requestedMovieIds.contains(AppJourneyTestData.trendingMovie.id)
        }
        detailRepository.requestedMovieIds shouldBeEqualTo
            listOf(AppJourneyTestData.trendingMovie.id)
        composeRule.onNodeWithContentDescription(string(SR.string.navigate_back)).performClick()
        composeRule.onNodeWithText(AppJourneyTestData.trendingMovie.title).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(SR.string.navigate_back)).performClick()
        composeRule.onNodeWithText(AppJourneyTestData.nowPlayingMovie.title).assertIsDisplayed()
    }

    @Test
    fun givenProfile_whenSettingsOpenedAndClosed_thenExistingTabGraphIsRestored() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)
        launch()
        val profile = string(SR.string.profile_title)

        composeRule.onNodeWithText(profile).performClick()
        composeRule.onNodeWithText(string(SR.string.profile_app_settings)).performClick()
        composeRule.onNodeWithText(string(SR.string.settings_title)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(SR.string.navigate_back)).performClick()

        composeRule.onNodeWithText(string(SR.string.profile_app_settings)).assertIsDisplayed()
        composeRule.onNodeWithText(profile).assertIsDisplayed()
    }

    @Test
    fun givenSearchMovie_whenClickedTwice_thenLifecycleGuardCreatesOneDetailDestination() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)
        launch()
        val title = AppJourneyTestData.nowPlayingMovie.title
        val movieNode = composeRule.onNode(hasText(title) and hasClickAction())

        movieNode.performTouchInput {
            click()
            click()
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            detailRepository.requestedMovieIds.isNotEmpty()
        }
        detailRepository.requestedMovieIds shouldBeEqualTo
            listOf(AppJourneyTestData.nowPlayingMovie.id)
        composeRule.onNodeWithContentDescription(string(SR.string.navigate_back)).performClick()
        composeRule.onAllNodesWithText(title).onFirst().assertIsDisplayed()
    }
}
