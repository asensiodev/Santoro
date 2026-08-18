package com.asensiodev.feature.searchmovies.impl.presentation.mapper

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.domain.model.MovieLibraryStatus
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi

internal fun Movie.toUi(libraryStatuses: Map<Int, MovieLibraryStatus> = emptyMap()): MovieUi =
    MovieUi(
        id = id,
        title = title,
        posterPath = posterPath?.let { BASE_POSTER_URL + it },
        backdropPath = backdropPath?.let { BASE_BACKDROP_URL + it },
        voteAverage = voteAverage,
        genreIds = genres.map { it.id },
        libraryStatus = libraryStatuses[id],
    )

internal fun List<Movie>.toUiList(
    libraryStatuses: Map<Int, MovieLibraryStatus> = emptyMap(),
): List<MovieUi> = distinctBy { movie -> movie.id }.map { movie -> movie.toUi(libraryStatuses) }

internal fun List<MovieUi>.withLibraryStatuses(
    libraryStatuses: Map<Int, MovieLibraryStatus>,
): List<MovieUi> = map { movie -> movie.copy(libraryStatus = libraryStatuses[movie.id]) }

private const val BASE_POSTER_URL = "https://image.tmdb.org/t/p/w500"
private const val BASE_BACKDROP_URL = "https://image.tmdb.org/t/p/w780"
