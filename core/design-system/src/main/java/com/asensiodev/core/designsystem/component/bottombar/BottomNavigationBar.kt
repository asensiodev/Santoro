package com.asensiodev.core.designsystem.component.bottombar

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
fun BottomNavigationBar(items: List<BottomNavItem>) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.isSelected,
                onClick = item.onClick,
                icon = { Icon(item.icon, null) },
                label = { Text(stringResource(item.labelRes)) },
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(
        items =
            listOf(
                BottomNavItem(
                    icon = AppIcons.SearchIcon,
                    labelRes = SR.string.search_movies,
                    isSelected = true,
                    onClick = {},
                ),
                BottomNavItem(
                    icon = AppIcons.WatchedMoviesIcon,
                    labelRes = SR.string.watched_movies,
                    isSelected = false,
                    onClick = {},
                ),
                BottomNavItem(
                    icon = AppIcons.WatchlistIcon,
                    labelRes = SR.string.watchlist,
                    isSelected = false,
                    onClick = {},
                ),
            ),
    )
}
