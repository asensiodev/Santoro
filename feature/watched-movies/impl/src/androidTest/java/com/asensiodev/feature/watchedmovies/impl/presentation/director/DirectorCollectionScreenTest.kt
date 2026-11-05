package com.asensiodev.feature.watchedmovies.impl.presentation.director

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm
import kotlinx.coroutines.awaitCancellation
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import com.asensiodev.santoro.core.stringresources.R as SR

@OptIn(DelicateCoilApi::class)
@RunWith(AndroidJUnit4::class)
class DirectorCollectionScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val intents = mutableListOf<DirectorCollectionIntent>()
    private var clickedMovieId: Int? = null
    private var profileClicks = 0
    private var backClicks = 0
    private var imagesFail = false
    private lateinit var previousImageLoader: ImageLoader
    private lateinit var imageLoader: ImageLoader

    @Before
    fun setUp() {
        previousImageLoader = SingletonImageLoader.get(composeRule.activity)
        imageLoader =
            ImageLoader
                .Builder(composeRule.activity)
                .components {
                    add(
                        Interceptor { chain ->
                            if (imagesFail) {
                                ErrorResult(null, chain.request, IOException("Offline"))
                            } else {
                                awaitCancellation()
                            }
                        },
                    )
                }.build()
        SingletonImageLoader.setUnsafe(imageLoader)
    }

    @After
    fun tearDown() {
        SingletonImageLoader.setUnsafe(previousImageLoader)
        imageLoader.shutdown()
    }

    @Test
    fun givenPartialCollection_whenShown_thenShowsSummaryAndPercentage() {
        setScreen(contentState())

        composeRule
            .onNodeWithText(
                string(SR.string.director_collection_progress, 1, 3),
            ).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                string(SR.string.director_collection_percent, 33),
            ).assertIsDisplayed()
    }

    @Test
    fun givenFilms_whenShown_thenAnnouncesWatchedAndNotWatchedStates() {
        setScreen(contentState())

        composeRule
            .onNodeWithText(
                MEMENTO,
            ).assert(hasState(string(SR.string.director_collection_watched)))
        composeRule
            .onNodeWithText(
                INCEPTION,
            ).assert(hasState(string(SR.string.director_collection_not_watched)))
    }

    @Test
    fun givenUnwatchedFilm_whenClicked_thenOpensItsDetail() {
        setScreen(contentState())

        composeRule.onNodeWithText(INCEPTION).performClick()

        composeRule.runOnIdle { clickedMovieId shouldBeEqualTo 2 }
    }

    @Test
    fun givenHeader_whenNameAndBackAreClicked_thenOpensProfileAndGoesBack() {
        setScreen(contentState())

        composeRule.onNodeWithText(NAME).performClick()
        composeRule.onNodeWithContentDescription(string(SR.string.navigate_back)).performClick()

        composeRule.runOnIdle {
            profileClicks shouldBeEqualTo 1
            backClicks shouldBeEqualTo 1
        }
    }

    @Test
    fun givenDirectorPhoto_whenShown_thenShowsItNextToTheName() {
        setScreen(contentState().copy(profilePath = "/nolan.jpg"))

        composeRule.onNodeWithTag(DIRECTOR_PHOTO_TAG, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText(NAME).assertIsDisplayed()
    }

    @Test
    fun givenDirectorPhotoFailsToLoad_whenShown_thenShowsOnlyTheName() {
        imagesFail = true
        setScreen(contentState().copy(profilePath = "/nolan.jpg"))

        composeRule.waitUntil {
            composeRule.onAllNodesWithTag(DIRECTOR_PHOTO_TAG, true).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText(NAME).assertIsDisplayed()
    }

    @Test
    fun givenNoDirectorPhoto_whenShown_thenShowsOnlyTheName() {
        setScreen(contentState())

        composeRule.onNodeWithTag(DIRECTOR_PHOTO_TAG, useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(NAME).assertIsDisplayed()
    }

    @Test
    fun givenShowPendingOn_whenSwitchIsClicked_thenRequestsHidingPending() {
        setScreen(contentState())

        composeRule
            .onNodeWithText(
                string(SR.string.director_collection_show_pending),
            ).assertIsOn()
            .performClick()

        composeRule.runOnIdle {
            intents shouldBeEqualTo listOf(DirectorCollectionIntent.ChangeShowPending(false))
        }
    }

    @Test
    fun givenPendingHidden_whenShown_thenOnlyWatchedFilmsRemainWithTheTotal() {
        setScreen(contentState(showPending = false))

        composeRule.onNodeWithText(MEMENTO).assertIsDisplayed()
        composeRule.onNodeWithText(INCEPTION).assertDoesNotExist()
        composeRule
            .onNodeWithText(
                string(SR.string.director_collection_progress, 1, 3),
            ).assertIsDisplayed()
    }

    @Test
    fun givenFilmographyLoading_whenShown_thenShowsWatchedFilmsAndAProgressIndicator() {
        setScreen(localState(FilmographyState.LOADING))

        composeRule.onNodeWithText(MEMENTO).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                pluralString(SR.plurals.director_collection_watched_count, 1),
            ).assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(
                string(SR.string.director_collection_loading),
            ).assertIsDisplayed()
    }

    @Test
    fun givenFilmographyError_whenRetryIsClicked_thenKeepsWatchedFilmsAndRequestsRetry() {
        setScreen(localState(FilmographyState.ERROR))

        composeRule.onNodeWithText(MEMENTO).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.director_collection_error)).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.error_message_retry_button)).performClick()

        composeRule.runOnIdle {
            intents shouldBeEqualTo listOf(DirectorCollectionIntent.RetryFilmography)
        }
    }

    private fun setScreen(state: DirectorCollectionUiState) {
        composeRule.setContent {
            SantoroTheme {
                DirectorCollectionScreen(
                    uiState = state,
                    onProcess = { intents += it },
                    onBackClick = { backClicks++ },
                    onMovieClick = { clickedMovieId = it },
                    onDirectorProfileClick = { profileClicks++ },
                )
            }
        }
    }

    private fun contentState(showPending: Boolean = true) =
        DirectorCollectionUiState(
            directorName = NAME,
            films =
                listOf(
                    DirectorFilm(1, MEMENTO, null, "2000-09-05", isWatched = true),
                    DirectorFilm(2, INCEPTION, null, "2010-07-16", isWatched = false),
                    DirectorFilm(3, "Oppenheimer", null, "2023-07-19", isWatched = false),
                ),
            watchedCount = 1,
            totalCount = 3,
            filmographyState = FilmographyState.CONTENT,
            showPending = showPending,
        )

    private fun localState(filmographyState: FilmographyState) =
        DirectorCollectionUiState(
            directorName = NAME,
            films = listOf(DirectorFilm(1, MEMENTO, null, "2000-09-05", isWatched = true)),
            watchedCount = 1,
            totalCount = null,
            filmographyState = filmographyState,
        )

    private fun hasState(value: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    private fun string(
        id: Int,
        vararg args: Any,
    ): String = composeRule.activity.getString(id, *args)

    private fun pluralString(
        id: Int,
        count: Int,
    ): String = composeRule.activity.resources.getQuantityString(id, count, count)

    private companion object {
        const val NAME = "Christopher Nolan"
        const val MEMENTO = "Memento"
        const val INCEPTION = "Inception"
    }
}
