package com.noirsonora.santoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noirsonora.core.navigation.Route
import com.noirsonora.core_ui.ui.theme.SantoroTheme
import com.noirsonora.login_presentation.LoginScreen
import com.noirsonora.onboarding_presentation.welcome.WelcomeScreen
import com.noirsonora.santoro.navigation.navigate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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
                        LoginScreen(onNavigate = navController::navigate)
                    }
                }
            }
        }
    }
}