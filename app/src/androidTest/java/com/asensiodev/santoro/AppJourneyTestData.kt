package com.asensiodev.santoro

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.SantoroUser

object AppJourneyTestData {
    const val DEEP_LINK_MOVIE_ID = 71019

    val authenticatedUser =
        SantoroUser(
            uid = "phase7-user",
            email = "phase7@example.invalid",
            displayName = "Phase Seven",
            photoUrl = null,
            isAnonymous = false,
        )

    val deepLinkMovie = movie(DEEP_LINK_MOVIE_ID, "Phase Seven Detail")
    val nowPlayingMovie = movie(71020, "Phase Seven Now Playing")
    val trendingMovie = movie(71021, "Phase Seven Trending")
    val popularMovie = movie(71022, "Phase Seven Popular")
    val topRatedMovie = movie(71023, "Phase Seven Top Rated")
    val upcomingMovie = movie(71024, "Phase Seven Upcoming")
    val watchedMovie = movie(71025, "Phase Seven Watched", isWatched = true, watchedAt = 300L)
    val watchlistMovie = movie(71026, "Phase Seven Watchlist", isInWatchlist = true)
    val dashboardMovies =
        listOf(nowPlayingMovie, trendingMovie, popularMovie, topRatedMovie, upcomingMovie)
    val databaseMovies = listOf(watchedMovie, watchlistMovie)
    val moviesById = (dashboardMovies + databaseMovies + deepLinkMovie).associateBy(Movie::id)

    fun movie(
        id: Int,
        title: String = "Phase Seven Movie $id",
        isWatched: Boolean = false,
        isInWatchlist: Boolean = false,
        watchedAt: Long? = null,
    ) = Movie(
        id = id,
        title = title,
        overview = "Deterministic Phase 7 overview",
        posterPath = null,
        backdropPath = null,
        releaseDate = "2026-08-14",
        popularity = 10.0,
        voteAverage = 8.0,
        voteCount = 100,
        genres = listOf(Genre(id = 18, name = "Drama")),
        genreIds = listOf(18),
        productionCountries = emptyList(),
        runtime = 120,
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
        updatedAt = 1_700_000_000_000L,
    )
}
