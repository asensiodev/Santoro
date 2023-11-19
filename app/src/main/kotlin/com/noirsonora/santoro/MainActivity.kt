package com.noirsonora.santoro

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
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
import com.noirsonora.onboarding_presentation.welcome.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().setKeepOnScreenCondition {
            !splashViewModel.isLoading.value
        }

        setContent {
            SantoroTheme {
                val startDestination by splashViewModel.startDestination
                val navController = rememberNavController()
                Surface {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        //Log.d("START_DESTINATION", startDestination)
                        composable(Route.WELCOME) {
                            OnboardingScreen(onNavigate = navController::navigateAndPopBackstack)
                        }
                        composable(Route.LOGIN) {
                            LoginScreen(onNavigate = navController::navigate)
                        }
                    }
                }
            }
        }
    }
}