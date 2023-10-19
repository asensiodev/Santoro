package com.noirsonora.santoro.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noirsonora.core.navigation.Route
import com.noirsonora.onboarding_presentation.welcome.WelcomeScreen
import com.noirsonora.santoro.presentation.navigation.navigate
import com.noirsonora.santoro.presentation.ui.theme.SantoroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SantoroTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Route.WELCOME
                ) {
                    composable(Route.WELCOME) {
                        WelcomeScreen(onNavigate = navController::navigate)
                    }
                    composable(Route.LOGIN) {

                    }
                    composable(Route.MOVIE_LIST) {

                    }
                }
            }
        }
    }
}