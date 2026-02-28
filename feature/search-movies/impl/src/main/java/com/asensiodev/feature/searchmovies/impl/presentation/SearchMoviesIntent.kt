package com.asensiodev.feature.searchmovies.impl.presentation

internal sealed interface SearchMoviesIntent {
    data object LoadInitialData : SearchMoviesIntent
    data object Refresh : SearchMoviesIntent
    data class UpdateQuery(
        val query: String,
    ) : SearchMoviesIntent
    data class SelectGenre(
        val genreId: Int,
    ) : SearchMoviesIntent
    data object ClearGenre : SearchMoviesIntent
    data object SearchWithoutGenreFilter : SearchMoviesIntent
    data object LoadMoreSearchResults : SearchMoviesIntent
    data object LoadMorePopularMovies : SearchMoviesIntent
}
