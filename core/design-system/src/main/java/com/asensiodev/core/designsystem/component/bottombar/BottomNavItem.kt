package com.asensiodev.core.designsystem.component.bottombar

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.santoro.core.stringresources.R

enum class BottomNavItem(
    val icon: ImageVector,
    @StringRes val label: Int,
) {
    SEARCH_MOVIES(
        icon = AppIcons.SearchMoviesIcon,
        label = R.string.search_movies,
    ),
    WATCHED_MOVIES(
        icon = AppIcons.WatchedMoviesIcon,
        label = R.string.watched_movies,
    ),
    WATCHLIST(
        icon = AppIcons.WatchlistIcon,
        label = R.string.watchlist,
    ),
}
