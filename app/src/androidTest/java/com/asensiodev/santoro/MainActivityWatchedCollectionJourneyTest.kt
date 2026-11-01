package com.asensiodev.santoro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import com.asensiodev.santoro.core.stringresources.R as SR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityWatchedCollectionJourneyTest : BaseAppJourneyTest() {
    @Test
    fun givenDirectorGrouping_whenProfileOpenedAndClosed_thenCollectionStateIsRetained() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)
        launch()
        composeRule.onNodeWithText(string(SR.string.watched_movies)).performClick()
        composeRule
            .onNodeWithContentDescription(
                string(SR.string.watched_view_mode_list),
            ).performClick()
        composeRule.onNodeWithText(string(SR.string.watched_group_director)).performClick()

        composeRule
            .onNode(hasText(AppJourneyTestData.DIRECTOR_NAME) and hasClickAction())
            .performClick()

        composeRule.onNodeWithText("Journey biography").assertIsDisplayed()
        personRepository.requestedPersonIds shouldBeEqualTo listOf(AppJourneyTestData.DIRECTOR_ID)
        composeRule.onNodeWithContentDescription(string(SR.string.navigate_back)).performClick()
        composeRule.onNodeWithText(string(SR.string.watched_group_director)).assertIsSelected()
        composeRule
            .onNodeWithContentDescription(
                string(SR.string.watched_view_mode_list),
            ).assertIsSelected()
        composeRule.onNodeWithText(AppJourneyTestData.watchedMovie.title).assertIsDisplayed()
    }
}
