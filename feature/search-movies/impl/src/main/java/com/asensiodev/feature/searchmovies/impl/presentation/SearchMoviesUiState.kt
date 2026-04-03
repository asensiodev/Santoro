package com.asensiodev.feature.searchmovies.impl.presentation

import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi

internal sealed interface SearchScreenState {
    data object Loading : SearchScreenState
    data object Content : SearchScreenState
    data class Error(
        val message: String,
    ) : SearchScreenState
    data object Empty : SearchScreenState
}

internal data class SearchMoviesUiState(
    val query: String = "",
    val screenState: SearchScreenState = SearchScreenState.Loading,
    val nowPlayingMovies: List<MovieUi> = emptyList(),
    val searchMovieResults: List<MovieUi> = emptyList(),
    val popularMovies: List<MovieUi> = emptyList(),
    val topRatedMovies: List<MovieUi> = emptyList(),
    val upcomingMovies: List<MovieUi> = emptyList(),
    val trendingMovies: List<MovieUi> = emptyList(),
    val isSearchLoadingMore: Boolean = false,
    val isPopularLoadingMore: Boolean = false,
    val currentSearchPage: Int = 1,
    val currentPopularPage: Int = 1,
    val isSearchEndReached: Boolean = false,
    val isPopularEndReached: Boolean = false,
    val selectedGenreId: Int? = null,
    val isShowingStaleData: Boolean = false,
    val isRefreshing: Boolean = false,
    val recentSearches: List<String> = emptyList(),
    val trendingSuggestions: List<String> = emptyList(),
    val isFieldFocused: Boolean = false,
) {
    val hasSearchResults: Boolean get() = searchMovieResults.isNotEmpty()
}
