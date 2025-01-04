package com.asensiodev.santoro.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import com.asensiodev.core.designsystem.component.bottombar.BottomNavItem
import com.asensiodev.core.designsystem.component.scaffold.SantoroScaffold
import com.asensiodev.core.designsystem.component.topbar.SantoroTopAppBar
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.santoro.navigation.NavigationScreens
import com.asensiodev.santoro.navigation.SantoroNavHost
import com.asensiodev.santoro.navigation.TopLevelDestination
import com.asensiodev.santoro.navigation.toBottomNavItem
import com.asensiodev.santoro.navigation.toNavRoute
import com.asensiodev.santoro.navigation.toTopLevelDestination
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.asensiodev.santoro.core.stringresources.R as SR

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navigationScreens: NavigationScreens

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SantoroTheme {
                val appState = rememberSantoroAppState()
                val bottomNavItems = TopLevelDestination.entries.map { it.toBottomNavItem() }
                val selectedBottomNavItem = appState.currentTopLevelDestination?.toBottomNavItem()
//                val currentBackStackEntry =
//                    appState.navController.currentBackStackEntryAsState()
//                val currentDestination = currentBackStackEntry.value?.destination

                val title =
                    when (selectedBottomNavItem) {
                        BottomNavItem.SEARCH_MOVIES ->
                            getString(
                                SR.string.search_movies_top_bar_title,
                            )

                        BottomNavItem.WATCHED_MOVIES ->
                            getString(
                                SR.string.watched_movies_top_bar_title,
                            )

                        BottomNavItem.WATCHLIST ->
                            getString(
                                SR.string.watchlist_top_bar_title,
                            )

                        else -> getString(SR.string.movie_detail_top_bar_title)
                    }

                val showBackButton = appState.currentTopLevelDestination == null
                SantoroScaffold(
                    topBar = {
                        SantoroTopAppBar(
                            title = title,
                            onBackClick =
                                if (showBackButton) {
                                    { appState.navController.popBackStack() }
                                } else {
                                    null
                                },
                        )
                    },
                    bottomNavItems = bottomNavItems,
                    selectedBottomNavItem = selectedBottomNavItem,
                    onBottomNavItemSelected = { bottomNavItem ->
                        appState.navController.popBackStack()
                        appState.navigateToTopLevelDestination(
                            bottomNavItem.toTopLevelDestination().toNavRoute(),
                        )
                    },
                ) {
                    SantoroNavHost(
                        navController = appState.navController,
                        navigationScreens = navigationScreens,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}
