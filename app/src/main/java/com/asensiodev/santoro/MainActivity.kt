package com.asensiodev.santoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.asensiodev.core.designsystem.component.bottombar.BottomNavigationBar
import com.asensiodev.core.designsystem.component.scaffold.SantoroScaffold
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.santoro.navigation.NavigationScreens
import com.asensiodev.santoro.navigation.SantoroNavHost
import com.asensiodev.santoro.navigation.TopLevelDestination
import com.asensiodev.santoro.navigation.toBottomNavItem
import com.asensiodev.santoro.navigation.toNavRoute
import com.asensiodev.santoro.presentation.rememberSantoroAppState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navigationScreens: NavigationScreens

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            SantoroTheme {
                val appState = rememberSantoroAppState()
                val currentTopLevel = appState.currentTopLevelDestination

                val bottomNavItems =
                    TopLevelDestination.entries.map { destination ->
                        destination.toBottomNavItem(
                            isSelected = destination == currentTopLevel,
                            onClick = {
                                appState.navigateToTopLevelDestination(
                                    destination.toNavRoute(),
                                )
                            },
                        )
                    }

                SantoroScaffold(
                    topBar = {},
                    bottomBar = {
                        if (currentTopLevel != null) {
                            BottomNavigationBar(items = bottomNavItems)
                        }
                    },
                ) {
                    SantoroNavHost(
                        navController = appState.navController,
                        navigationScreens = navigationScreens,
                    )
                }
            }
        }
    }
}
