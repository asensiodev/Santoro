package com.asensiodev.santoro.navigation

import com.asensiodev.core.designsystem.component.bottombar.BottomNavItem

fun TopLevelDestination.toBottomNavItem(): BottomNavItem =
    when (this) {
        TopLevelDestination.SEARCH_MOVIES -> BottomNavItem.SEARCH_MOVIES
        TopLevelDestination.WATCHED_MOVIES -> BottomNavItem.WATCHED_MOVIES
        TopLevelDestination.WATCHLIST -> BottomNavItem.WATCHLIST
    }

fun BottomNavItem.toTopLevelDestination(): TopLevelDestination =
    when (this) {
        BottomNavItem.SEARCH_MOVIES -> TopLevelDestination.SEARCH_MOVIES
        BottomNavItem.WATCHED_MOVIES -> TopLevelDestination.WATCHED_MOVIES
        BottomNavItem.WATCHLIST -> TopLevelDestination.WATCHLIST
        else -> TopLevelDestination.SEARCH_MOVIES
    }
