package com.asensiodev.feature.watchlist.impl.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.feature.watchlist.impl.domain.usecase.GetWatchlistMoviesUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.RemoveFromWatchlistUseCase
import com.asensiodev.feature.watchlist.impl.domain.usecase.SearchWatchlistMoviesUseCase
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import com.asensiodev.santoro.core.sync.scheduler.WorkManagerSyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchlistFeatureTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var repository: FakeDatabaseRepository
    private lateinit var viewModel: WatchlistMoviesViewModel

    @Before
    fun setUp() {
        repository =
            FakeDatabaseRepository(
                movies =
                    listOf(
                        movie(id = 1, title = "Inception"),
                        movie(id = 2, title = "Arrival"),
                    ),
            )
        val dispatchers = TestDispatcherProvider()
        viewModel =
            WatchlistMoviesViewModel(
                getWatchlistMoviesUseCase = GetWatchlistMoviesUseCase(repository, dispatchers),
                searchWatchlistMoviesUseCase =
                    SearchWatchlistMoviesUseCase(
                        repository,
                        dispatchers,
                    ),
                removeFromWatchlistUseCase =
                    RemoveFromWatchlistUseCase(
                        repository,
                        dispatchers,
                    ),
                syncScheduler =
                    WorkManagerSyncScheduler(
                        ApplicationProvider.getApplicationContext(),
                    ),
            )
    }

    @Test
    fun givenWatchlistMovies_whenMovieIsSelected_thenNavigatesWithMovieId() {
        var selectedMovieId: Int? = null
        composeRule.setContent {
            SantoroTheme {
                WatchlistMoviesRoute(
                    onMovieClick = { selectedMovieId = it },
                    viewModel = viewModel,
                )
            }
        }

        composeRule.onNodeWithText("Inception").assertIsDisplayed().performClick()

        composeRule.runOnIdle {
            selectedMovieId shouldBeEqualTo 1
        }
    }

    @Test
    fun givenWatchlistMovies_whenQueryIsEntered_thenShowsMatchingMovies() {
        composeRule.setContent {
            SantoroTheme {
                WatchlistMoviesRoute(
                    onMovieClick = {},
                    viewModel = viewModel,
                )
            }
        }

        composeRule
            .onNode(hasSetTextAction())
            .performTextInput("arrival")

        composeRule.waitUntil(timeoutMillis = 2_000) {
            composeRule.onAllNodesWithText("Inception").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText("Arrival").assertIsDisplayed()
        composeRule.onNodeWithText("Inception").assertDoesNotExist()
    }

    private fun movie(
        id: Int,
        title: String,
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
        isWatched = false,
        isInWatchlist = true,
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

    override suspend fun updateMovieState(movie: Movie): Result<Boolean> {
        movies.value = movies.value.map { if (it.id == movie.id) movie else it }
        return Result.success(true)
    }

    override suspend fun removeFromWatchlist(movieId: Int): Result<Boolean> {
        movies.value =
            movies.value.map { movie ->
                if (movie.id == movieId) movie.copy(isInWatchlist = false) else movie
            }
        return Result.success(true)
    }

    override suspend fun getMoviesForSync(): Result<List<Movie>> = Result.success(movies.value)

    override suspend fun upsertMovieFromSync(
        movieId: Int,
        title: String,
        posterPath: String?,
        genres: String,
        runtime: Int?,
        isWatched: Boolean,
        isInWatchlist: Boolean,
        watchedAt: Long?,
        updatedAt: Long,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun updateMovieSyncState(
        movieId: Int,
        isWatched: Boolean,
        isInWatchlist: Boolean,
        watchedAt: Long?,
        updatedAt: Long,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun clearAllUserData(): Result<Unit> {
        movies.value = emptyList()
        return Result.success(Unit)
    }
}
