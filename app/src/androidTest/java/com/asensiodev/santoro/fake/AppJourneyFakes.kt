@file:Suppress("EXPOSED_PARAMETER_TYPE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.asensiodev.santoro.fake

import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.feature.moviedetail.impl.domain.repository.MovieDetailRepository
import com.asensiodev.feature.searchmovies.impl.domain.model.FetchPolicy
import com.asensiodev.feature.searchmovies.impl.domain.repository.RecentSearchesRepository
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import com.asensiodev.santoro.AppJourneyTestData
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import com.asensiodev.santoro.core.sync.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class FakeAuthRepository : AuthRepository {
    private val userState = MutableStateFlow<SantoroUser?>(null)
    override val currentUser: Flow<SantoroUser?> = userState
    val signOutCalls = AtomicInteger()

    fun setUser(user: SantoroUser?) {
        userState.value = user
    }

    override suspend fun signInAnonymously(): Result<SantoroUser> =
        Result.success(AppJourneyTestData.authenticatedUser)

    override suspend fun signInWithGoogle(idToken: String): Result<SantoroUser> =
        Result.success(AppJourneyTestData.authenticatedUser)

    override suspend fun linkWithGoogle(idToken: String): Result<SantoroUser> =
        Result.success(AppJourneyTestData.authenticatedUser)

    override suspend fun signOut() {
        signOutCalls.incrementAndGet()
        userState.value = null
    }

    override suspend fun deleteAccount(): Result<Unit> {
        userState.value = null
        return Result.success(Unit)
    }

    fun reset(user: SantoroUser? = null) {
        signOutCalls.set(0)
        userState.value = user
    }
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
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

    fun reset() {
        hasSeenGuestOnboarding.value = true
        hasSeenDetailTooltip.value = true
        theme.value = ThemeOption.SYSTEM
    }
}

class FakeSearchMoviesRepository : SearchMoviesRepository {
    override fun searchMovies(
        query: String,
        page: Int,
        fetchPolicy: FetchPolicy,
    ): Flow<Result<List<Movie>>> =
        pageResult(
            page,
            AppJourneyTestData.dashboardMovies.filter { movie ->
                movie.title.contains(query, ignoreCase = true)
            },
        )

    override fun getNowPlayingMovies(
        page: Int,
        fetchPolicy: FetchPolicy,
    ) = pageResult(page, listOf(AppJourneyTestData.nowPlayingMovie))

    override fun getPopularMovies(
        page: Int,
        fetchPolicy: FetchPolicy,
    ) = pageResult(page, listOf(AppJourneyTestData.popularMovie))

    override fun getTopRatedMovies(
        page: Int,
        fetchPolicy: FetchPolicy,
    ) = pageResult(page, listOf(AppJourneyTestData.topRatedMovie))

    override fun getUpcomingMovies(
        page: Int,
        fetchPolicy: FetchPolicy,
    ) = pageResult(page, listOf(AppJourneyTestData.upcomingMovie))

    override fun getTrendingMovies(
        page: Int,
        fetchPolicy: FetchPolicy,
    ) = pageResult(page, listOf(AppJourneyTestData.trendingMovie))

    override fun getMoviesByGenre(
        genreId: Int,
        page: Int,
    ) = pageResult(
        page,
        AppJourneyTestData.dashboardMovies.filter { movie -> genreId in movie.genreIds },
    )

    private fun pageResult(
        page: Int,
        movies: List<Movie>,
    ): Flow<Result<List<Movie>>> = flowOf(Result.success(if (page == 1) movies else emptyList()))
}

class FakeRecentSearchesRepository : RecentSearchesRepository {
    private val searches = MutableStateFlow<List<String>>(emptyList())

    override fun getRecentSearches(): Flow<List<String>> = searches

    override suspend fun saveSearch(query: String) {
        searches.value =
            listOf(query) +
            searches.value.filterNot { existing ->
                existing.equals(query, ignoreCase = true)
            }
    }

    override suspend fun clearAll() {
        searches.value = emptyList()
    }

    fun reset() {
        searches.value = emptyList()
    }
}

class FakeMovieDetailRepository : MovieDetailRepository {
    val requestedMovieIds = CopyOnWriteArrayList<Int>()

    override fun getMovieDetail(id: Int): Flow<Result<Movie?>> {
        requestedMovieIds += id
        return flowOf(
            Result.success(
                AppJourneyTestData.moviesById[id] ?: AppJourneyTestData.movie(id),
            ),
        )
    }

    override suspend fun updateMovieState(movie: Movie): Result<Boolean> = Result.success(true)

    fun reset() {
        requestedMovieIds.clear()
    }
}

class FakeDatabaseRepository : DatabaseRepository {
    private val movies = MutableStateFlow(AppJourneyTestData.databaseMovies)

    override fun getWatchedMovies(): Flow<Result<List<Movie>>> =
        movies.map { values -> Result.success(values.filter(Movie::isWatched)) }

    override fun getWatchlistMovies(): Flow<Result<List<Movie>>> =
        movies.map { values -> Result.success(values.filter(Movie::isInWatchlist)) }

    override suspend fun getMovieById(movieId: Int): Result<Movie?> =
        Result.success(movies.value.firstOrNull { movie -> movie.id == movieId })

    override fun searchWatchedMoviesByTitle(query: String): Flow<Result<List<Movie>>> =
        movies.map { values ->
            Result.success(
                values.filter { movie ->
                    movie.isWatched && movie.title.contains(query, ignoreCase = true)
                },
            )
        }

    override fun searchWatchlistMoviesByTitle(query: String): Flow<Result<List<Movie>>> =
        movies.map { values ->
            Result.success(
                values.filter { movie ->
                    movie.isInWatchlist && movie.title.contains(query, ignoreCase = true)
                },
            )
        }

    override suspend fun updateMovieState(movie: Movie): Result<Boolean> {
        movies.value =
            movies.value.map { existing -> if (existing.id == movie.id) movie else existing }
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

    fun reset() {
        movies.value = AppJourneyTestData.databaseMovies
    }
}

class FakeSyncRepository : SyncRepository {
    val pendingUploadUserIds = CopyOnWriteArrayList<String>()

    override suspend fun uploadMovie(
        uid: String,
        movieId: Int,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun uploadPendingChanges(uid: String): Result<Unit> {
        pendingUploadUserIds += uid
        return Result.success(Unit)
    }

    override suspend fun downloadAndMerge(uid: String): Result<Unit> = Result.success(Unit)

    fun reset() {
        pendingUploadUserIds.clear()
    }
}

class FakeRemoteConfigProvider : RemoteConfigProvider {
    override suspend fun initialize() = Unit

    override fun getStringParameter(remoteConfigName: RemoteConfigName): String = ""
}
