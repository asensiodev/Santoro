package com.asensiodev.santoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
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
import com.asensiodev.santoro.presentation.onboarding.GuestOnboardingBottomSheet
import com.asensiodev.settings.impl.presentation.navigation.settingsRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop

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
                        val isAuthenticated = uiState is MainActivityUiState.Authenticated
                        val startDestination =
                            if (isAuthenticated) {
                                TabHost
                            } else {
                                LoginRoute
                            }

                        SantoroApp(
                            startDestination = startDestination,
                            isAuthenticated = isAuthenticated,
                        )

                        if (uiState is MainActivityUiState.Authenticated &&
                            (uiState as MainActivityUiState.Authenticated).showGuestOnboarding
                        ) {
                            GuestOnboardingBottomSheet(
                                onDismissRequest = viewModel::dismissGuestOnboarding,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Suppress("FunctionNaming")
fun SantoroApp(
    startDestination: Any,
    isAuthenticated: Boolean,
) {
    val mainNavController = rememberNavController()
    val currentIsAuthenticated by rememberUpdatedState(isAuthenticated)

    LaunchedEffect(mainNavController) {
        snapshotFlow { currentIsAuthenticated }
            .drop(1)
            .collect { isAuth ->
                if (isAuth) {
                    mainNavController.navigate(TabHost) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                } else {
                    mainNavController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
    }

    NavHost(
        navController = mainNavController,
        startDestination = startDestination,
    ) {
        loginScreen()

        composable<TabHost> {
            SantoroMainTabComponent(mainNavController = mainNavController)
        }

        movieDetailRoute(
            onBackClicked = { mainNavController.popBackStack() },
        )

        settingsRoute(
            onBackClicked = { mainNavController.popBackStack() },
        )
    }
}
