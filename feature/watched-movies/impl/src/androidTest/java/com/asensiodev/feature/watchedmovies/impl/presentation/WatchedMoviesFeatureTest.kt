package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
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
            )
    }

    @Test
    fun givenWatchedMovies_whenFeatureLoads_thenShowsStatsAndNavigatesWithMovieId() {
        var selectedMovieId: Int? = null
        composeRule.setContent {
            SantoroTheme {
                WatchedMoviesRoute(
                    onMovieClick = { selectedMovieId = it },
                    viewModel = viewModel,
                )
            }
        }

        val totalMovies = composeRule.activity.getString(SR.string.watched_stat_total_value, 2)
        composeRule.onNodeWithText(totalMovies).assertIsDisplayed()
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
