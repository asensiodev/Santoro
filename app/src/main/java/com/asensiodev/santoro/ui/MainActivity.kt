package com.asensiodev.santoro.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.asensiodev.core.designsystem.component.bottombar.BottomNavigationBar
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.santoro.navigation.NavigationScreens
import com.asensiodev.santoro.navigation.SantoroNavHost
import com.asensiodev.santoro.navigation.TopLevelDestination
import com.asensiodev.santoro.navigation.toBottomNavItem
import com.asensiodev.santoro.navigation.toTopLevelDestination
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
                Scaffold(
                    bottomBar = {
                        val selectedDSItem = appState.currentTopLevelDestination?.toBottomNavItem()

                        BottomNavigationBar(
                            items = bottomNavItems,
                            selectedItem = selectedDSItem,
                            onItemSelected = { Item ->
                                appState.navigateToTopLevelDestination(
                                    Item.toTopLevelDestination(),
                                )
                            },
                        )
                    },
                ) { innerPadding ->
                    SantoroNavHost(
                        navController = appState.navController,
                        navigationScreens = navigationScreens,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
