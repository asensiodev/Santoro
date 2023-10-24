package com.noirsonora.santoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noirsonora.core.navigation.Route
import com.noirsonora.login_presentation.LoginScreen
import com.noirsonora.santoro.navigation.navigate
import com.noirsonora.santoro.splash_screen.SplashScreen
import com.noirsonora.santoro.ui.theme.SantoroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SantoroTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Route.SPLASH_SCREEN
                ) {
                    composable(Route.SPLASH_SCREEN) {
                        SplashScreen(navController = navController)
                    }
                    composable(Route.LOGIN) {
                        LoginScreen(onNavigate = navController::navigate)
                    }
                    composable(Route.MOVIE_LIST) {

                    }
                }
            }
        }
    }
}