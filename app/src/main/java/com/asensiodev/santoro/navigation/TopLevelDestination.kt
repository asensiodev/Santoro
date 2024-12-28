package com.asensiodev.santoro.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.asensiodev.core.designsystem.theme.AppIcons
import kotlin.reflect.KClass
import com.asensiodev.santoro.core.stringresources.R as SR

enum class TopLevelDestination(
    val icon: ImageVector,
    @StringRes val label: Int,
    val route: KClass<*>,
) {
    SEARCH_MOVIES(
        icon = AppIcons.SearchMoviesIcon,
        label = SR.string.search_movies,
        route = NavRoutes.SearchMovies::class,
    ),
    WATCHED_MOVIES(
        icon = AppIcons.WatchedMoviesIcon,
        label = SR.string.watched_movies,
        route = NavRoutes.WatchedMovies::class,
    ),
    WATCHLIST(
        icon = AppIcons.WatchlistIcon,
        label = SR.string.watchlist,
        route = NavRoutes.Watchlist::class,
    ),
}
