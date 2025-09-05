package com.asensiodev.santoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.asensiodev.core.designsystem.component.bottombar.BottomNavItem
import com.asensiodev.core.designsystem.component.bottombar.BottomNavigationBar
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.moviedetail.impl.presentation.navigation.movieDetailRoute
import com.asensiodev.santoro.navigation.SantoroTabNavGraph
import com.asensiodev.santoro.navigation.SantoroTabs
import com.asensiodev.santoro.navigation.TabHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            SantoroTheme {
                val mainNavController = rememberNavController()
                val tabNavController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        val currentDestination by tabNavController.currentBackStackEntryAsState()

                        BottomNavigationBar(
                            items =
                                SantoroTabs.map { tab ->
                                    val routeName = tab.route::class.qualifiedName.orEmpty()
                                    BottomNavItem(
                                        icon = tab.icon,
                                        labelRes = tab.labelRes,
                                        isSelected =
                                            currentDestination?.destination?.route?.startsWith(
                                                routeName,
                                            ) == true,
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
                    NavHost(
                        navController = mainNavController,
                        startDestination = TabHost,
                    ) {
                        composable<TabHost> {
                            SantoroTabNavGraph(
                                navController = tabNavController,
                                mainNavController = mainNavController,
                                paddingValues = innerPadding,
                            )
                        }

                        movieDetailRoute()
                    }
                }
            }
        }
    }
}
