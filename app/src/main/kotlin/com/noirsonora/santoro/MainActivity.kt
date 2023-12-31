package com.noirsonora.santoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noirsonora.core.navigation.Route
import com.noirsonora.core_ui.ui.theme.SantoroTheme
import com.noirsonora.login_presentation.LoginScreen
import com.noirsonora.onboarding_presentation.welcome.OnboardingScreen
import com.noirsonora.santoro.navigation.navigate
import com.noirsonora.santoro.navigation.navigateAndPopBackstack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var keepShowingSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            keepShowingSplashScreen
        }
        setContent {
            SantoroTheme {
                val navController = rememberNavController()
                Surface {
                    NavHost(
                        navController = navController,
                        startDestination = Route.LOGIN
                    ) {
                        composable(Route.LOGIN) {
                            LoginScreen(
                                onScreenReady = {
                                    keepShowingSplashScreen = false
                                },
                                onNavigate = navController::navigate
                            )
                        }
                        composable(Route.ONBOARDING) {
                            OnboardingScreen(onNavigate = navController::navigateAndPopBackstack)
                        }
                    }
                }
            }
        }
    }
}