package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.core.domain.usecase.ObserveHasSeenDetailTooltipUseCase
import com.asensiodev.core.domain.usecase.SetDetailTooltipSeenUseCase
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import com.asensiodev.feature.moviedetail.impl.domain.usecase.GetMovieDetailUseCase
import com.asensiodev.feature.moviedetail.impl.domain.usecase.UpdateMovieStateUseCase
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.asensiodev.santoro.core.stringresources.R as SR

@RunWith(AndroidJUnit4::class)
class MovieDetailFeatureTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var repository: RetryMovieDetailRepository
    private lateinit var viewModel: MovieDetailViewModel

    @Before
    fun setUp() {
        repository = RetryMovieDetailRepository(movie())
        val dispatchers = TestDispatcherProvider()
        val preferencesRepository = FakeUserPreferencesRepository()
        viewModel =
            MovieDetailViewModel(
                getMovieDetailUseCase = GetMovieDetailUseCase(repository, dispatchers),
                updateMovieStateUseCase = UpdateMovieStateUseCase(repository, dispatchers),
                syncScheduler =
                    WorkManagerSyncScheduler(
                        ApplicationProvider.getApplicationContext(),
                    ),
                observeHasSeenDetailTooltipUseCase =
                    ObserveHasSeenDetailTooltipUseCase(preferencesRepository),
                setDetailTooltipSeenUseCase =
                    SetDetailTooltipSeenUseCase(preferencesRepository),
            )
    }

    @Test
    fun givenMovieDetailLoadFails_whenRetryIsSelected_thenLoadsRequestedMovie() {
        composeRule.setContent {
            SantoroTheme {
                MovieDetailRoute(
                    movieId = MOVIE_ID,
                    onBackClicked = {},
                    viewModel = viewModel,
                )
            }
        }

        val retry = composeRule.activity.getString(SR.string.error_content_button)
        composeRule.onNodeWithText(retry).assertIsDisplayed().performClick()

        composeRule.waitUntil {
            composeRule.onAllNodesWithText(MOVIE_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        repository.requestedMovieIds shouldBeEqualTo listOf(MOVIE_ID, MOVIE_ID)
    }

    private fun movie() =
        Movie(
            id = MOVIE_ID,
            title = MOVIE_TITLE,
            overview = "A deterministic movie overview",
            posterPath = null,
            backdropPath = null,
            releaseDate = "2016-11-11",
            popularity = 10.0,
            voteAverage = 8.0,
            voteCount = 100,
            genres = listOf(Genre(id = 18, name = "Drama")),
            productionCountries = emptyList(),
            isWatched = false,
            isInWatchlist = false,
        )

    private companion object {
        const val MOVIE_ID = 42
        const val MOVIE_TITLE = "Arrival"
    }
}

private class RetryMovieDetailRepository(
    private val movie: Movie,
) : MovieDetailRepository {
    val requestedMovieIds = mutableListOf<Int>()

    override fun getMovieDetail(id: Int): Flow<Result<Movie?>> =
        flow {
            requestedMovieIds += id
            if (requestedMovieIds.size == 1) {
                emit(Result.failure(IllegalStateException("Movie detail unavailable")))
            } else {
                emit(Result.success(movie))
            }
        }

    override suspend fun updateMovieState(movie: Movie): Result<Boolean> = Result.success(true)
}

private class FakeUserPreferencesRepository : UserPreferencesRepository {
    override val hasSeenGuestOnboarding = MutableStateFlow(true)
    override val hasSeenDetailTooltip = MutableStateFlow(true)
    override val theme = MutableStateFlow(ThemeOption.SYSTEM)

    override suspend fun setHasSeenGuestOnboarding(hasSeen: Boolean) {
        hasSeenGuestOnboarding.value = hasSeen
    }

    override suspend fun setHasSeenDetailTooltip(hasSeen: Boolean) {
        hasSeenDetailTooltip.value = hasSeen
    }

    override suspend fun setTheme(option: ThemeOption) {
        theme.value = option
    }
}
