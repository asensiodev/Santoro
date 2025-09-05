package com.asensiodev.santoro.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.feature.searchmovies.api.navigation.SearchMoviesRoute
import com.asensiodev.feature.watchedmovies.api.navigation.WatchedMoviesRoute
import com.asensiodev.feature.watchlist.api.navigation.WatchlistRoute
import com.asensiodev.santoro.core.stringresources.R
import kotlinx.serialization.Serializable

@Serializable
data object TabHost

data class SantoroTabModel(
    @StringRes val titleRes: Int,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val route: Any,
)

val SantoroTabs =
    listOf(
        SantoroTabModel(
            titleRes = R.string.search_movies_top_bar_title,
            labelRes = R.string.search_movies,
            icon = AppIcons.SearchIcon,
            route = SearchMoviesRoute,
        ),
        SantoroTabModel(
            titleRes = R.string.watched_movies_top_bar_title,
            labelRes = R.string.watched_movies,
            icon = AppIcons.WatchedMoviesIcon,
            route = WatchedMoviesRoute,
        ),
        SantoroTabModel(
            titleRes = R.string.watchlist_top_bar_title,
            labelRes = R.string.watchlist,
            icon = AppIcons.WatchlistIcon,
            route = WatchlistRoute,
        ),
    )
