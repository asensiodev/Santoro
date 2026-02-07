package com.asensiodev.santoro.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.feature.searchmovies.api.navigation.SearchMoviesRoute
import com.asensiodev.feature.watchedmovies.api.navigation.WatchedMoviesRoute
import com.asensiodev.feature.watchlist.api.navigation.WatchlistRoute
import com.asensiodev.santoro.core.stringresources.R
import com.asensiodev.settings.api.navigation.ProfileRoute
import kotlinx.serialization.Serializable

@Serializable
data object TabHost

data class SantoroTabModel(
    @StringRes val titleRes: Int,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Any,
    val isRootDestination: Boolean = false,
)

val SantoroTabs =
    listOf(
        SantoroTabModel(
            titleRes = R.string.search_movies_top_bar_title,
            labelRes = R.string.search_movies,
            selectedIcon = AppIcons.Home,
            unselectedIcon = AppIcons.HomeOutlined,
            route = SearchMoviesRoute,
        ),
        SantoroTabModel(
            titleRes = R.string.watched_movies_top_bar_title,
            labelRes = R.string.watched_movies,
            selectedIcon = AppIcons.Watched,
            unselectedIcon = AppIcons.WatchedOutlined,
            route = WatchedMoviesRoute,
        ),
        SantoroTabModel(
            titleRes = R.string.watchlist_top_bar_title,
            labelRes = R.string.watchlist,
            selectedIcon = AppIcons.Watchlist,
            unselectedIcon = AppIcons.WatchlistOutlined,
            route = WatchlistRoute,
        ),
        SantoroTabModel(
            titleRes = R.string.profile_title,
            labelRes = R.string.profile_title,
            selectedIcon = AppIcons.Profile,
            unselectedIcon = AppIcons.ProfileOutlined,
            route = ProfileRoute,
        ),
    )
