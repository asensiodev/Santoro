package com.asensiodev.santoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.moviedetail.impl.presentation.navigation.movieDetailRoute
import com.asensiodev.santoro.navigation.SantoroMainTabComponent
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

                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = mainNavController,
                        startDestination = TabHost,
                    ) {
                        composable<TabHost> {
                            SantoroMainTabComponent(mainNavController = mainNavController)
                        }

                        movieDetailRoute(
                            onBackClicked = { mainNavController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
