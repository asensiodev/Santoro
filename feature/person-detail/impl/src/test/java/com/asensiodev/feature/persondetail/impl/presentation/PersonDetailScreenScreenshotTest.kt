package com.asensiodev.feature.persondetail.impl.presentation

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.feature.persondetail.api.navigation.FilmographySection
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class PersonDetailScreenScreenshotTest {
    private var previousLocale = Locale.getDefault()

    @Before
    fun useDeterministicDates() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(previousLocale)
    }

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_3, theme = "Theme.Santoro", showSystemUi = false)

    @Test
    fun contentLight() = capture()

    @Test
    fun contentDark() = capture(dark = true)

    @Test
    fun missingBiographyLight() = capture(biography = "")

    @Test
    fun missingBiographyDark() = capture(dark = true, biography = "")

    @Test
    fun creditsErrorLight() = capture(credits = PersonLoadState.Error)

    @Test
    fun creditsErrorDark() = capture(dark = true, credits = PersonLoadState.Error)

    @Test
    fun directorLight() = capture(department = "Directing")

    @Test
    fun directorDark() = capture(dark = true, department = "Directing")

    @Test
    fun largeFilmographyLight() = capture(movieCount = 9)

    @Test
    fun largeFilmographyDark() = capture(dark = true, movieCount = 9)

    @Test
    fun completeFilmographyLight() = captureComplete()

    @Test
    fun completeFilmographyDark() = captureComplete(dark = true)

    private fun captureComplete(dark: Boolean = false) {
        paparazzi.snapshot {
            SantoroTheme(darkTheme = dark) {
                PersonFilmographyScreen(
                    PersonDetailUiState(
                        person = Person(7, "Alex Morgan", "", null, null, null, null),
                        profileState = PersonLoadState.Content,
                        filmographyState = PersonLoadState.Content,
                        filmography =
                            PersonFilmography(
                                listOf(
                                    PersonMovieCredit(1, "The Long Journey", null, "2024-03-15", listOf("Lead")),
                                    PersonMovieCredit(2, "Another Journey", null, "2024-01-01", listOf("Guest")),
                                    PersonMovieCredit(3, "An Earlier Journey", null, "2023-01-01", listOf("Lead")),
                                    PersonMovieCredit(4, "An Undated Journey", null, null, listOf("Lead")),
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
    }

    private fun capture(
        dark: Boolean = false,
        biography: String = "An actor and director whose work spans several decades.",
        credits: PersonLoadState = PersonLoadState.Content,
        department: String? = null,
        movieCount: Int = 1,
    ) {
        paparazzi.snapshot {
            SantoroTheme(darkTheme = dark) {
                PersonDetailScreen(
                    state =
                        PersonDetailUiState(
                            person =
                                Person(
                                    7,
                                    "Alex Morgan",
                                    biography,
                                    null,
                                    "1970-05-12",
                                    null,
                                    "Madrid, Spain",
                                    department,
                                ),
                            profileState = PersonLoadState.Content,
                            filmographyState = credits,
                            filmography = profileFilmography(movieCount),
                        ),
                    onBackClicked = {},
                    onMovieClicked = {},
                    onRetryProfile = {},
                    onRetryFilmography = {},
                )
            }
        }
    }

    private fun profileFilmography(movieCount: Int) =
        PersonFilmography(
            acting =
                listOf(PersonMovieCredit(42, "The Long Journey", null, "2024-03-15", listOf("Alex"))) +
                    (2..movieCount).map {
                        PersonMovieCredit(it, "Another Movie $it", null, "2020-01-01", listOf("Lead"))
                    },
            crew = listOf(PersonMovieCredit(42, "The Long Journey", null, "2024-03-15", listOf("Director"))),
        )
}
