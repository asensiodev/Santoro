package com.asensiodev.santoro

import androidx.compose.ui.test.assertIsDisplayed
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
class MainActivityAuthenticationJourneyTest : BaseAppJourneyTest() {
    @Test
    fun givenUnauthenticatedUser_whenLaunched_thenLoginHostIsShown() {
        launch()

        composeRule
            .onNodeWithText(
                string(SR.string.login_anonymous_login_button),
            ).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.login_google_login_button)).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.watched_movies)).assertDoesNotExist()
    }

    @Test
    fun givenAuthenticatedUser_whenLaunched_thenTabHostIsShown() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)

        launch()

        composeRule.onNodeWithText(string(SR.string.search_movies)).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.watched_movies)).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.watchlist)).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.profile_title)).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                string(SR.string.login_anonymous_login_button),
            ).assertDoesNotExist()
    }

    @Test
    fun givenAuthenticatedSettings_whenLogoutClicked_thenLoginReplacesBackStack() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)
        val scenario = launch()
        val profile = string(SR.string.profile_title)

        composeRule.onNodeWithText(profile).performClick()
        composeRule
            .onNodeWithText(
                formattedString(
                    SR.string.profile_user_greeting,
                    AppJourneyTestData.authenticatedUser.displayName,
                ),
            ).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.profile_app_settings)).performClick()
        composeRule.onNodeWithText(string(SR.string.settings_logout)).performClick()

        composeRule
            .onNodeWithText(
                string(SR.string.login_anonymous_login_button),
            ).assertIsDisplayed()
        authRepository.signOutCalls.get() shouldBeEqualTo 1
        syncRepository.pendingUploadUserIds shouldBeEqualTo
            listOf(AppJourneyTestData.authenticatedUser.uid)
        scenario.onActivity { activity -> activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNodeWithText(profile).assertDoesNotExist()
    }
}
