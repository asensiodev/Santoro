package com.asensiodev.feature.persondetail.impl.presentation

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.feature.persondetail.api.navigation.FilmographySection
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import com.asensiodev.santoro.core.stringresources.R as SR

@RunWith(AndroidJUnit4::class)
class PersonDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun givenMissingBiography_whenFilmographyMovieClicked_thenIdentityIsDelivered() {
        var movieId: Int? = null
        composeRule.setContent {
            SantoroTheme {
                PersonDetailScreen(
                    state = state(),
                    onBackClicked = {},
                    onMovieClicked = { movieId = it },
                    onRetryProfile = {},
                    onRetryFilmography = {},
                )
            }
        }
        composeRule.onNodeWithText(string(SR.string.person_biography_empty)).assertIsDisplayed()
        composeRule
            .onNodeWithText("Movie")
            .performScrollTo()
            .assertHasClickAction()
            .performClick()
        composeRule.runOnIdle { movieId shouldBeEqualTo 42 }
        composeRule.onNodeWithText("2024").assertIsDisplayed()
    }

    @Test
    fun givenLongBiography_whenExpanded_thenReadLessIsAvailable() {
        composeRule.setContent {
            SantoroTheme {
                PersonDetailScreen(
                    state =
                        state().copy(
                            person =
                                state().person?.copy(
                                    biography = "A long biography. ".repeat(100),
                                ),
                        ),
                    onBackClicked = {},
                    onMovieClicked = {},
                    onRetryProfile = {},
                    onRetryFilmography = {},
                )
            }
        }
        composeRule
            .onNodeWithText(
                string(SR.string.person_biography_more),
            ).performScrollTo()
            .performClick()
        composeRule
            .onNodeWithText(
                string(SR.string.person_biography_less),
            ).performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule
            .onNodeWithText(
                string(SR.string.person_biography_more),
            ).performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun givenCreditsError_whenRetryClicked_thenBiographyRemainsAndRetryIsDelivered() {
        var retries = 0
        composeRule.setContent {
            SantoroTheme {
                PersonDetailScreen(
                    state = state().copy(filmographyState = PersonLoadState.Error),
                    onBackClicked = {},
                    onMovieClicked = {},
                    onRetryProfile = {},
                    onRetryFilmography = { retries++ },
                )
            }
        }
        composeRule.onNodeWithText(string(SR.string.person_biography_empty)).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                string(SR.string.error_content_button),
            ).performScrollTo()
            .performClick()
        composeRule.runOnIdle { retries shouldBeEqualTo 1 }
    }

    @Test
    fun givenProfileLoading_whenBackClicked_thenLoadingIsVisibleAndBackIsDelivered() {
        var backClicks = 0
        render(PersonDetailUiState(), onBack = { backClicks++ })
        composeRule
            .onNode(
                SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo),
            ).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.person_biography)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(string(SR.string.navigate_back)).performClick()
        composeRule.runOnIdle { backClicks shouldBeEqualTo 1 }
    }

    @Test
    fun givenProfileError_whenRetryClicked_thenProfileRetryIsDeliveredWithoutCredits() {
        var retries = 0
        render(
            PersonDetailUiState(profileState = PersonLoadState.Error),
            onRetryProfile = { retries++ },
        )
        composeRule.onNodeWithText(string(SR.string.person_detail_error)).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.person_filmography)).assertDoesNotExist()
        composeRule
            .onNodeWithText(
                string(SR.string.error_content_button),
            ).assertHasClickAction()
            .performClick()
        composeRule.runOnIdle { retries shouldBeEqualTo 1 }
    }

    @Test
    fun givenProfileError_whenBackClicked_thenBackRemainsAvailable() {
        var backClicks = 0
        render(PersonDetailUiState(profileState = PersonLoadState.Error), onBack = { backClicks++ })
        composeRule
            .onNodeWithContentDescription(
                string(SR.string.navigate_back),
            ).assertHasClickAction()
            .performClick()
        composeRule.runOnIdle { backClicks shouldBeEqualTo 1 }
    }

    @Test
    fun givenEmptyCredits_whenRendered_thenEmptyMessageReplacesBothSections() {
        render(state().copy(filmography = PersonFilmography(emptyList(), emptyList())))
        composeRule
            .onNodeWithText(
                string(SR.string.person_filmography_empty),
            ).performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.person_acting)).assertDoesNotExist()
        composeRule.onNodeWithText(string(SR.string.person_crew)).assertDoesNotExist()
        composeRule.onNodeWithText(string(SR.string.error_content_button)).assertDoesNotExist()
    }

    @Test
    fun givenOnlyCrewCredits_whenMovieClicked_thenCrewIdentityIsDeliveredWithoutActingSection() {
        var selected: Int? = null
        render(
            state().copy(
                filmography =
                    PersonFilmography(
                        emptyList(),
                        listOf(
                            PersonMovieCredit(
                                84,
                                "Directed Movie",
                                null,
                                null,
                                listOf("Director", "Writer"),
                            ),
                        ),
                    ),
            ),
            onMovie = { selected = it },
        )
        composeRule.onNodeWithText(string(SR.string.person_acting)).assertDoesNotExist()
        composeRule
            .onNodeWithText(
                string(SR.string.person_crew),
            ).performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Director · Writer").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Directed Movie").assertHasClickAction().performClick()
        composeRule.runOnIdle { selected shouldBeEqualTo 84 }
    }

    @Test
    fun givenOnlyActingCredits_whenRendered_thenCrewSectionIsOmitted() {
        render(state())
        composeRule
            .onNodeWithText(
                string(SR.string.person_acting),
            ).performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.person_crew)).assertDoesNotExist()
    }

    @Test
    fun givenShortBiography_whenRendered_thenExpansionControlsAreOmitted() {
        render(
            state().copy(person = Person(7, "Actor", "A short biography.", null, null, null, null)),
        )
        composeRule.onNodeWithText("A short biography.").assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.person_biography_more)).assertDoesNotExist()
        composeRule.onNodeWithText(string(SR.string.person_biography_less)).assertDoesNotExist()
    }

    @Test
    fun givenCreditsLoading_whenRendered_thenBiographyRemainsWithProgressAndNoMovies() {
        render(state().copy(filmographyState = PersonLoadState.Loading))
        composeRule.onNodeWithText(string(SR.string.person_biography_empty)).assertIsDisplayed()
        composeRule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Movie").assertDoesNotExist()
        composeRule.onNodeWithText(string(SR.string.error_content_button)).assertDoesNotExist()
    }

    @Test
    fun givenCreditsError_whenRecovered_thenMoviesReplaceError() {
        val current = mutableStateOf(state().copy(filmographyState = PersonLoadState.Error))
        composeRule.setContent {
            SantoroTheme {
                PersonDetailScreen(current.value, {}, {}, {}, { current.value = state() })
            }
        }
        composeRule
            .onNodeWithText(
                string(SR.string.person_filmography_error),
            ).performScrollTo()
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(
                string(SR.string.error_content_button),
            ).performScrollTo()
            .performClick()
        composeRule.onNodeWithText("Movie").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.person_filmography_error)).assertDoesNotExist()
        composeRule
            .onNodeWithText(
                string(SR.string.person_biography_empty),
            ).performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun givenExpandedBiography_whenSavedStateRestored_thenBiographyRemainsExpanded() {
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent {
            SantoroTheme {
                PersonDetailScreen(
                    state().copy(
                        person =
                            Person(
                                7,
                                "Actor",
                                "A long biography. ".repeat(100),
                                null,
                                null,
                                null,
                                null,
                            ),
                    ),
                    {},
                    {},
                    {},
                    {},
                )
            }
        }
        composeRule
            .onNodeWithText(
                string(SR.string.person_biography_more),
            ).performScrollTo()
            .performClick()
        restoration.emulateSavedInstanceStateRestore()
        composeRule
            .onNodeWithText(
                string(SR.string.person_biography_less),
            ).performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun givenSpanishConfiguration_whenBiographyIsMissing_thenLocalizedEmptyMessageIsShown() {
        val configuration =
            Configuration(composeRule.activity.resources.configuration).apply {
                setLocale(Locale.forLanguageTag("es-ES"))
            }
        val context = composeRule.activity.createConfigurationContext(configuration)
        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides context,
                LocalConfiguration provides configuration,
            ) {
                SantoroTheme {
                    PersonDetailScreen(state(), {}, {}, {}, {})
                }
            }
        }
        composeRule
            .onNodeWithText(
                context.getString(SR.string.person_biography_empty),
            ).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(SR.string.person_biography),
            ).assertIsDisplayed()
        composeRule.onNodeWithText("No biography available in this language.").assertDoesNotExist()
    }

    @Test
    fun givenDirectorWithActingCredits_whenRendered_thenBehindCameraAppearsFirst() {
        render(
            state().copy(
                person = state().person?.copy(knownForDepartment = "Directing"),
                filmography =
                    PersonFilmography(
                        state().filmography.acting,
                        listOf(
                            PersonMovieCredit(84, "Directed Movie", null, null, listOf("Director")),
                        ),
                    ),
            ),
        )
        composeRule.onNodeWithText(string(SR.string.person_filmography)).performScrollTo()
        val crew =
            composeRule
                .onNodeWithText(
                    string(SR.string.person_crew),
                ).fetchSemanticsNode()
                .boundsInRoot.top
        val acting =
            composeRule
                .onNodeWithText(
                    string(SR.string.person_acting),
                ).fetchSemanticsNode()
                .boundsInRoot.top
        (crew < acting) shouldBeEqualTo true
    }

    @Test
    fun givenNineActingMovies_whenSeeAllClicked_thenSectionIsDeliveredAndPreviewStopsAtEight() {
        var selected: FilmographySection? = null
        composeRule.setContent {
            SantoroTheme {
                PersonDetailScreen(
                    state().copy(
                        filmography =
                            PersonFilmography(
                                (1..9).map {
                                    PersonMovieCredit(
                                        it,
                                        "Movie $it",
                                        null,
                                        null,
                                        emptyList(),
                                    )
                                },
                                emptyList(),
                            ),
                    ),
                    {},
                    {},
                    {},
                    {},
                    onSeeAllClicked = { selected = it },
                )
            }
        }
        composeRule
            .onNodeWithText(
                string(SR.string.browse_see_all),
            ).performScrollTo()
            .performClick()
        selected shouldBeEqualTo FilmographySection.ACTING
        composeRule.onNodeWithTag("filmography-ACTING").performScrollToNode(hasText("Movie 8"))
        composeRule.onNodeWithText("Movie 8").assertExists()
        assertThrows(AssertionError::class.java) {
            composeRule.onNodeWithTag("filmography-ACTING").performScrollToNode(hasText("Movie 9"))
        }
    }

    @Test
    fun givenEightMovies_whenRendered_thenSeeAllIsOmitted() {
        render(
            state().copy(
                filmography =
                    PersonFilmography(
                        (1..8).map { PersonMovieCredit(it, "Movie $it", null, null, emptyList()) },
                        emptyList(),
                    ),
            ),
        )
        composeRule.onNodeWithText(string(SR.string.browse_see_all)).assertDoesNotExist()
    }

    @Test
    fun givenMoviesSharingAYear_whenCompleteListRendered_thenOneYearHeadingPrecedesTheirCards() {
        composeRule.setContent {
            SantoroTheme {
                PersonFilmographyScreen(
                    state().copy(
                        filmography =
                            PersonFilmography(
                                listOf(
                                    PersonMovieCredit(
                                        1,
                                        "Latest Movie",
                                        null,
                                        "2024-12-01",
                                        listOf("Lead"),
                                    ),
                                    PersonMovieCredit(
                                        2,
                                        "Another Movie",
                                        null,
                                        "2024-01-01",
                                        emptyList(),
                                    ),
                                    PersonMovieCredit(
                                        3,
                                        "Earlier Movie",
                                        null,
                                        "2023-01-01",
                                        emptyList(),
                                    ),
                                ),
                                emptyList(),
                            ),
                    ),
                    FilmographySection.ACTING,
                    {},
                    {},
                    {},
                )
            }
        }
        val heading =
            composeRule.onNode(
                hasText("2024") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading),
            )
        heading.assertIsDisplayed()
        composeRule.onAllNodesWithText("2024", useUnmergedTree = true).assertCountEquals(1)
        val headingTop = heading.fetchSemanticsNode().boundsInRoot.top
        val firstTop =
            composeRule
                .onNodeWithText(
                    "Latest Movie",
                ).fetchSemanticsNode()
                .boundsInRoot.top
        val secondTop =
            composeRule
                .onNodeWithText(
                    "Another Movie",
                ).fetchSemanticsNode()
                .boundsInRoot.top
        (headingTop < firstTop && firstTop < secondTop) shouldBeEqualTo true
        composeRule.onNodeWithText("2023").performScrollTo().assertIsDisplayed()
        val earlierHeadingTop =
            composeRule
                .onNodeWithText(
                    "2023",
                ).fetchSemanticsNode()
                .boundsInRoot.top
        val earlierMovieTop =
            composeRule
                .onNodeWithText(
                    "Earlier Movie",
                ).fetchSemanticsNode()
                .boundsInRoot.top
        (earlierHeadingTop < earlierMovieTop) shouldBeEqualTo true
    }

    @Test
    fun givenCrewYearsAndUndatedMovie_whenRendered_thenHeadingsPreserveOrderAndMovieClick() {
        var selected: Int? = null
        composeRule.setContent {
            SantoroTheme {
                PersonFilmographyScreen(
                    state().copy(
                        person = state().person?.copy(knownForDepartment = "Directing"),
                        filmography =
                            PersonFilmography(
                                emptyList(),
                                listOf(
                                    PersonMovieCredit(
                                        1,
                                        "Latest Direction",
                                        null,
                                        "2024-01-01",
                                        listOf("Director"),
                                    ),
                                    PersonMovieCredit(
                                        2,
                                        "Earlier Direction",
                                        null,
                                        "2023-01-01",
                                        listOf("Director"),
                                    ),
                                    PersonMovieCredit(
                                        3,
                                        "Undated Direction",
                                        null,
                                        null,
                                        listOf("Director"),
                                    ),
                                ),
                            ),
                    ),
                    FilmographySection.CREW,
                    {},
                    { selected = it },
                    {},
                )
            }
        }
        val headingMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)
        val latestTop =
            composeRule
                .onNode(
                    hasText("2024") and headingMatcher,
                ).fetchSemanticsNode()
                .boundsInRoot.top
        val earlierTop =
            composeRule
                .onNode(
                    hasText("2023") and headingMatcher,
                ).fetchSemanticsNode()
                .boundsInRoot.top
        (latestTop < earlierTop) shouldBeEqualTo true
        composeRule.onAllNodesWithText("2024", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithText("2023", useUnmergedTree = true).assertCountEquals(1)
        val undated =
            composeRule.onNode(
                hasText(string(SR.string.person_release_date_unknown)) and headingMatcher,
            )
        undated.performScrollTo().assertIsDisplayed()
        val undatedTop = undated.fetchSemanticsNode().boundsInRoot.top
        val movie = composeRule.onNodeWithText("Undated Direction")
        (undatedTop < movie.fetchSemanticsNode().boundsInRoot.top) shouldBeEqualTo true
        movie.assertHasClickAction().performClick()
        selected shouldBeEqualTo 3
    }

    @Test
    fun givenCrewSection_whenCompleteListMovieClicked_thenCrewMovieIdentityIsDelivered() {
        var selected: Int? = null
        composeRule.setContent {
            SantoroTheme {
                PersonFilmographyScreen(
                    state().copy(
                        filmography =
                            PersonFilmography(
                                emptyList(),
                                listOf(
                                    PersonMovieCredit(
                                        84,
                                        "Directed Movie",
                                        null,
                                        null,
                                        listOf("Director"),
                                    ),
                                ),
                            ),
                    ),
                    FilmographySection.CREW,
                    {},
                    { selected = it },
                    {},
                )
            }
        }
        composeRule.onNodeWithText("Directed Movie").assertHasClickAction().performClick()
        selected shouldBeEqualTo 84
    }

    @Test
    fun givenEmptyCompleteList_whenRendered_thenEmptyMessageAndBackAreAvailable() {
        var back = 0
        composeRule.setContent {
            SantoroTheme {
                PersonFilmographyScreen(
                    state().copy(filmography = PersonFilmography(emptyList(), emptyList())),
                    FilmographySection.ACTING,
                    { back++ },
                    {},
                    {},
                )
            }
        }
        composeRule.onNodeWithText(string(SR.string.person_filmography_empty)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(SR.string.navigate_back)).performClick()
        back shouldBeEqualTo 1
    }

    @Test
    fun givenCompleteListError_whenRetryClicked_thenRetryIsDelivered() {
        var retry = 0
        composeRule.setContent {
            SantoroTheme {
                PersonFilmographyScreen(
                    state().copy(filmographyState = PersonLoadState.Error),
                    FilmographySection.ACTING,
                    {},
                    {},
                    { retry++ },
                )
            }
        }
        composeRule.onNodeWithText(string(SR.string.person_filmography_error)).assertIsDisplayed()
        composeRule.onNodeWithText(string(SR.string.error_content_button)).performClick()
        retry shouldBeEqualTo 1
    }

    @Test
    fun givenCompleteListLoading_whenRendered_thenProgressReplacesMovies() {
        composeRule.setContent {
            SantoroTheme {
                PersonFilmographyScreen(
                    state().copy(filmographyState = PersonLoadState.Loading),
                    FilmographySection.ACTING,
                    {},
                    {},
                    {},
                )
            }
        }
        composeRule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertIsDisplayed()
        composeRule.onNodeWithText("Movie").assertDoesNotExist()
    }

    private fun render(
        current: PersonDetailUiState,
        onBack: () -> Unit = {},
        onMovie: (Int) -> Unit = {},
        onRetryProfile: () -> Unit = {},
    ) {
        composeRule.setContent {
            SantoroTheme {
                PersonDetailScreen(current, onBack, onMovie, onRetryProfile, {})
            }
        }
    }

    private fun state() =
        PersonDetailUiState(
            person = Person(7, "Actor", "", null, null, null, null),
            profileState = PersonLoadState.Content,
            filmographyState = PersonLoadState.Content,
            filmography =
                PersonFilmography(
                    listOf(PersonMovieCredit(42, "Movie", null, "2024-01-01", listOf("Lead"))),
                    emptyList(),
                ),
        )

    private fun string(id: Int) = composeRule.activity.getString(id)
}
