package com.asensiodev.santoro.navigation

import com.asensiodev.core.designsystem.component.bottombar.BottomNavItem

fun TopLevelDestination.toBottomNavItem(
    isSelected: Boolean,
    onClick: () -> Unit,
): BottomNavItem =
    BottomNavItem(
        icon = this.icon,
        labelRes = this.labelRes,
        isSelected = isSelected,
        onClick = onClick,
    )

fun TopLevelDestination.toNavRoute(): NavRoutes =
    when (this) {
        TopLevelDestination.SEARCH_MOVIES -> NavRoutes.SearchMovies
        TopLevelDestination.WATCHED_MOVIES -> NavRoutes.WatchedMovies
        TopLevelDestination.WATCHLIST -> NavRoutes.Watchlist
    }
