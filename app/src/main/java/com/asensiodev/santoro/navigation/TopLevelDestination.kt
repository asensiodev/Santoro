// app/navigation/TopLevelDestination.kt
package com.asensiodev.santoro.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.santoro.core.stringresources.R
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val route: KClass<*>,
    @StringRes val titleRes: Int,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    SEARCH_MOVIES(
        route = NavRoutes.SearchMovies::class,
        titleRes = R.string.search_movies_top_bar_title,
        labelRes = R.string.search_movies,
        icon = AppIcons.SearchIcon,
    ),
    WATCHED_MOVIES(
        route = NavRoutes.WatchedMovies::class,
        titleRes = R.string.watched_movies_top_bar_title,
        labelRes = R.string.watched_movies,
        icon = AppIcons.WatchedMoviesIcon,
    ),
    WATCHLIST(
        route = NavRoutes.Watchlist::class,
        titleRes = R.string.watchlist_top_bar_title,
        labelRes = R.string.watchlist,
        icon = AppIcons.WatchlistIcon,
    ),
    ;

    companion object {
        val routes: Set<KClass<*>> = entries.map { destination -> destination.route }.toSet()
    }
}
