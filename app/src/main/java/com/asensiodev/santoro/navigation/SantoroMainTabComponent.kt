package com.asensiodev.santoro.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.asensiodev.core.designsystem.component.bottombar.BottomNavItem
import com.asensiodev.core.designsystem.component.bottombar.BottomNavigationBar

@Composable
fun SantoroMainTabComponent(mainNavController: NavHostController) {
    val tabNavController = rememberNavController()
    val currentDestination by tabNavController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                items =
                    SantoroTabs.map { tab ->
                        val routeName = tab.route::class.qualifiedName.orEmpty()

                        BottomNavItem(
                            icon = tab.icon,
                            labelRes = tab.labelRes,
                            isSelected =
                                currentDestination?.destination?.route?.startsWith(routeName) ==
                                    true,
                            onClick = {
                                tabNavController.navigate(tab.route) {
                                    popUpTo(tabNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    },
            )
        },
    ) { innerPadding ->
        SantoroTabNavGraph(
            navController = tabNavController,
            mainNavController = mainNavController,
            paddingValues = innerPadding,
        )
    }
}
