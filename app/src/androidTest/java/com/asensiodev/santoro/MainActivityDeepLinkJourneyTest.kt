package com.asensiodev.santoro

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import com.asensiodev.santoro.core.stringresources.R as SR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityDeepLinkJourneyTest : BaseAppJourneyTest() {
    @Test
    fun givenAuthenticatedMovieDeepLink_whenLaunched_thenItIsConsumedOnce() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)
        val intent =
            mainActivityIntent()
                .setAction(Intent.ACTION_VIEW)
                .setData(
                    Uri.parse(
                        "https://www.themoviedb.org/movie/" +
                            "${AppJourneyTestData.DEEP_LINK_MOVIE_ID}-phase-seven",
                    ),
                )

        launch(intent)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            detailRepository.requestedMovieIds.size == 1
        }
        detailRepository.requestedMovieIds shouldBeEqualTo
            listOf(AppJourneyTestData.DEEP_LINK_MOVIE_ID)
        composeRule
            .onAllNodesWithText(AppJourneyTestData.deepLinkMovie.title)
            .onFirst()
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(SR.string.navigate_back)).performClick()
        composeRule.onNodeWithText(AppJourneyTestData.nowPlayingMovie.title).assertIsDisplayed()
    }

    @Test
    fun givenRunningAuthenticatedApp_whenNewIntentArrives_thenMovieDetailOpensOnce() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)
        launch()
        val intent =
            Intent(Intent.ACTION_VIEW).setData(
                Uri.parse("https://themoviedb.org/movie/${AppJourneyTestData.DEEP_LINK_MOVIE_ID}"),
            )

        deliverNewIntent(intent)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            detailRepository.requestedMovieIds.size == 1
        }
        detailRepository.requestedMovieIds shouldBeEqualTo
            listOf(AppJourneyTestData.DEEP_LINK_MOVIE_ID)
    }

    @Test
    fun givenMalformedOrUnsupportedNewIntents_whenDelivered_thenSearchRemainsVisible() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)
        launch()
        val malformed =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://themoviedb.org/movie/not-a-number"))
        val unsupported =
            Intent(Intent.ACTION_VIEW, Uri.parse("custom://themoviedb.org/movie/71019"))

        deliverNewIntent(malformed)
        deliverNewIntent(unsupported)

        composeRule.onNodeWithText(AppJourneyTestData.nowPlayingMovie.title).assertIsDisplayed()
        detailRepository.requestedMovieIds.isEmpty() shouldBeEqualTo true
    }

    private fun deliverNewIntent(intent: Intent) {
        intent
            .setClass(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MainActivity::class.java,
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        InstrumentationRegistry.getInstrumentation().targetContext.startActivity(intent)
    }
}
