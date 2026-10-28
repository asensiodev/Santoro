package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedStatsUseCase
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.SearchWatchedMoviesUseCase
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.asensiodev.santoro.core.stringresources.R as SR

@RunWith(AndroidJUnit4::class)
class WatchedMoviesFeatureTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var viewModel: WatchedMoviesViewModel

    @Before
    fun setUp() {
        val repository =
            FakeDatabaseRepository(
                movies =
                    listOf(
                        movie(id = 1, title = "Inception", runtime = 120),
                        movie(id = 2, title = "Arrival", runtime = 120),
                    ),
            )
        val dispatchers = TestDispatcherProvider()
        viewModel =
            WatchedMoviesViewModel(
                getWatchedMoviesUseCase = GetWatchedMoviesUseCase(repository, dispatchers),
                getWatchedStatsUseCase = GetWatchedStatsUseCase(repository, dispatchers),
                searchWatchedMoviesUseCase = SearchWatchedMoviesUseCase(repository, dispatchers),
                userPreferencesRepository = FakeUserPreferencesRepository(),
            )
    }

    @Test
    fun givenWatchedMovies_whenFeatureLoads_thenShowsStatsAndNavigatesWithMovieId() {
        var selectedMovieId: Int? = null
        composeRule.setContent {
            SantoroTheme {
                WatchedMoviesRoute(
                    onMovieClick = { selectedMovieId = it },
                    onDirectorClick = {},
                    viewModel = viewModel,
                )
            }
        }

        val totalMovies = composeRule.activity.getString(SR.string.watched_stat_total_value, 2)
        composeRule.onAllNodesWithText(totalMovies).assertCountEquals(STATS_AND_GROUP_COUNT)
        composeRule.onNodeWithText("Inception").assertIsDisplayed().performClick()

        composeRule.runOnIdle {
            selectedMovieId shouldBeEqualTo 1
        }
    }

    private fun movie(
        id: Int,
        title: String,
        runtime: Int,
    ) = Movie(
        id = id,
        title = title,
        overview = "Overview",
        posterPath = null,
        backdropPath = null,
        releaseDate = "2010-01-01",
        popularity = 10.0,
        voteAverage = 8.0,
        voteCount = 100,
        genres = listOf(Genre(id = 18, name = "Drama")),
        productionCountries = emptyList(),
        runtime = runtime,
        isWatched = true,
        isInWatchlist = false,
        watchedAt = 1_700_000_000_000,
    )
}

private class FakeDatabaseRepository(
    movies: List<Movie>,
) : DatabaseRepository {
    private val movies = MutableStateFlow(movies)

    override fun getWatchedMovies(): Flow<Result<List<Movie>>> =
        movies.map { Result.success(it.filter(Movie::isWatched)) }

    override fun getWatchlistMovies(): Flow<Result<List<Movie>>> =
        movies.map { Result.success(it.filter(Movie::isInWatchlist)) }

    override suspend fun getMovieById(movieId: Int): Result<Movie?> =
        Result.success(movies.value.firstOrNull { it.id == movieId })

    override fun searchWatchedMoviesByTitle(query: String): Flow<Result<List<Movie>>> =
        movies.map { values ->
            Result.success(
                values.filter {
                    it.isWatched &&
                        it.title.contains(query, ignoreCase = true)
                },
            )
        }

    override fun searchWatchlistMoviesByTitle(query: String): Flow<Result<List<Movie>>> =
        movies.map { values ->
            Result.success(
                values.filter {
                    it.isInWatchlist &&
                        it.title.contains(query, ignoreCase = true)
                },
            )
        }
}

private class FakeUserPreferencesRepository : UserPreferencesRepository {
    override val hasSeenGuestOnboarding = MutableStateFlow(true)
    override val hasSeenDetailTooltip = MutableStateFlow(true)
    override val theme = MutableStateFlow(ThemeOption.SYSTEM)
    override val watchedViewMode = MutableStateFlow(WatchedViewMode.SHELF)
    override val watchedGrouping = MutableStateFlow(WatchedGrouping.DATE_WATCHED)

    override suspend fun setHasSeenGuestOnboarding(hasSeen: Boolean) {
        hasSeenGuestOnboarding.value = hasSeen
    }

    override suspend fun setHasSeenDetailTooltip(hasSeen: Boolean) {
        hasSeenDetailTooltip.value = hasSeen
    }

    override suspend fun setTheme(option: ThemeOption) {
        theme.value = option
    }

    override suspend fun setWatchedViewMode(mode: WatchedViewMode) {
        watchedViewMode.value = mode
    }

    override suspend fun setWatchedGrouping(grouping: WatchedGrouping) {
        watchedGrouping.value = grouping
    }
}

private const val STATS_AND_GROUP_COUNT = 2
