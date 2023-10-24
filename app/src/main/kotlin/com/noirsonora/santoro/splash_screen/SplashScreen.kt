package com.noirsonora.santoro.splash_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.noirsonora.core.navigation.Route
import com.noirsonora.core.util.SANTORO_APP_LOGO
import com.noirsonora.core_ui.LocalSplashScreenDimensions
import com.noirsonora.santoro.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds


@Composable
fun SplashScreen(
    navController: NavHostController
) {
    LaunchedEffect(
        key1 = Unit,
    ) {
        delay(3.seconds)
        navController.popBackStack()
        navController.navigate(Route.LOGIN)
    }
    Splash()
}

@Composable
fun Splash() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.santoro_app_logo),
            contentDescription = SANTORO_APP_LOGO,
            Modifier.size(
                LocalSplashScreenDimensions.current.splashScreenIconWidth,
                LocalSplashScreenDimensions.current.splashScreenIconHeight
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    Splash()
}