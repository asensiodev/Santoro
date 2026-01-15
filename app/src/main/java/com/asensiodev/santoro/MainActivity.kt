package com.asensiodev.santoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.asensiodev.api.navigation.LoginRoute
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.moviedetail.impl.presentation.navigation.movieDetailRoute
import com.asensiodev.login.impl.presentation.navigation.loginScreen
import com.asensiodev.santoro.navigation.SantoroMainTabComponent
import com.asensiodev.santoro.navigation.TabHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            splashScreen.setKeepOnScreenCondition {
                uiState is MainActivityUiState.Loading
            }

            SantoroTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (uiState !is MainActivityUiState.Loading) {
                        val startDestination =
                            if (uiState is MainActivityUiState.Authenticated) {
                                TabHost
                            } else {
                                LoginRoute
                            }

                        SantoroApp(startDestination = startDestination)
                    }
                }
            }
        }
    }
}

@Composable
fun SantoroApp(startDestination: Any) {
    val mainNavController = rememberNavController()

    NavHost(
        navController = mainNavController,
        startDestination = startDestination,
    ) {
        loginScreen(
            onSignInSuccess = {
                mainNavController.navigate(TabHost) {
                    popUpTo(LoginRoute) { inclusive = true }
                }
            },
        )

        composable<TabHost> {
            SantoroMainTabComponent(mainNavController = mainNavController)
        }

        movieDetailRoute(
            onBackClicked = { mainNavController.popBackStack() },
        )
    }
}
