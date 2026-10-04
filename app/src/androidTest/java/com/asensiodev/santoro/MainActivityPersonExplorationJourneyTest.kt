package com.asensiodev.santoro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.domain.model.PersonMovieCredit
import dagger.hilt.android.testing.HiltAndroidTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import com.asensiodev.santoro.core.stringresources.R as SR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityPersonExplorationJourneyTest : BaseAppJourneyTest() {
    @Test
    fun givenActor_whenFollowingFilmography_thenBackRestoresBothSources() {
        openMovie()
        actor().performScrollTo().performClick()
        composeRule.onNodeWithText("Journey biography").assertIsDisplayed()
        val movie = AppJourneyTestData.trendingMovie
        composeRule
            .onNode(
                hasText(movie.title) and hasClickAction(),
            ).performScrollTo()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            detailRepository.requestedMovieIds.contains(movie.id)
        }
        back()
        composeRule.onNode(hasText(movie.title) and hasClickAction()).assertIsDisplayed()
        back()
        actor().assertIsDisplayed()
        personRepository.requestedPersonIds shouldBeEqualTo listOf(AppJourneyTestData.ACTOR_ID)
    }

    @Test
    fun givenDirector_whenTapped_thenCorrectProfileOpens() {
        openMovie()
        composeRule
            .onNode(
                hasText(AppJourneyTestData.DIRECTOR_NAME) and hasClickAction(),
            ).performScrollTo()
            .performClick()
        composeRule.onNodeWithText("Journey biography").assertIsDisplayed()
        personRepository.requestedPersonIds shouldBeEqualTo listOf(AppJourneyTestData.DIRECTOR_ID)
    }

    @Test
    fun givenActor_whenTappedTwice_thenOnePersonDestinationIsOpened() {
        openMovie()
        actor().performScrollTo().performTouchInput {
            click()
            click()
        }
        composeRule.onNodeWithText("Journey biography").assertIsDisplayed()
        personRepository.requestedPersonIds shouldBeEqualTo listOf(AppJourneyTestData.ACTOR_ID)
        back()
        actor().assertIsDisplayed()
    }

    @Test
    fun givenFilmography_whenMovieTappedTwice_thenOneMovieDestinationIsOpened() {
        openMovie()
        actor().performScrollTo().performClick()
        val movie = AppJourneyTestData.trendingMovie
        composeRule
            .onNode(
                hasText(movie.title) and hasClickAction(),
            ).performScrollTo()
            .performTouchInput {
                click()
                click()
            }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            detailRepository.requestedMovieIds.contains(movie.id)
        }
        detailRepository.requestedMovieIds shouldBeEqualTo
            listOf(AppJourneyTestData.nowPlayingMovie.id, movie.id)
        back()
        composeRule.onNodeWithText("Journey biography").assertIsDisplayed()
    }

    @Test
    fun givenPersonProfile_whenActivityRecreates_thenIdentityAndContentAreRetained() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)
        val scenario = launch()
        composeRule.onNodeWithText(AppJourneyTestData.nowPlayingMovie.title).performClick()
        actor().performScrollTo().performClick()
        composeRule.onNodeWithText("Journey biography").assertIsDisplayed()
        scenario.recreate()
        composeRule.onNodeWithText("Journey biography").assertIsDisplayed()
        personRepository.requestedPersonIds shouldBeEqualTo listOf(AppJourneyTestData.ACTOR_ID)
    }

    @Test
    fun givenLargeFilmography_whenBrowsingAllAndReturning_thenScrollAndLoadedCreditsAreRetained() {
        val movie = AppJourneyTestData.nowPlayingMovie
        personRepository.extraMovies =
            (1..12).map { index ->
                PersonMovieCredit(
                    if (index == 12) movie.id else 1000 + index,
                    if (index == 12) movie.title else "Filmography movie $index",
                    null,
                    "2020-01-01",
                    listOf("Lead"),
                )
            }
        openMovie()
        actor().performScrollTo().performClick()
        composeRule.onNodeWithText(string(SR.string.browse_see_all)).performScrollTo()
        composeRule
            .onNodeWithTag(
                "filmography-ACTING",
            ).performScrollToNode(hasText("Filmography movie 7"))
        composeRule.onNodeWithText(string(SR.string.browse_see_all)).performClick()
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(movie.title))
        composeRule.onNode(hasText(movie.title) and hasClickAction()).performClick()
        composeRule.waitUntil(
            timeoutMillis = 5_000,
        ) { detailRepository.requestedMovieIds.size == 2 }
        back()
        composeRule.onNode(hasText(movie.title) and hasClickAction()).assertIsDisplayed()
        back()
        composeRule.onNodeWithText(string(SR.string.browse_see_all)).assertIsDisplayed()
        composeRule.onNodeWithText("Filmography movie 7").assertIsDisplayed()
        personRepository.requestedPersonIds shouldBeEqualTo listOf(AppJourneyTestData.ACTOR_ID)
        personRepository.requestedCreditIds shouldBeEqualTo listOf(AppJourneyTestData.ACTOR_ID)
    }

    private fun openMovie() {
        authRepository.setUser(AppJourneyTestData.authenticatedUser)
        launch()
        composeRule.onNodeWithText(AppJourneyTestData.nowPlayingMovie.title).performClick()
    }

    private fun actor() =
        composeRule.onNode(
            hasText(AppJourneyTestData.ACTOR_NAME) and hasClickAction(),
        )

    private fun back() =
        composeRule.onNodeWithContentDescription(string(SR.string.navigate_back)).performClick()
}
