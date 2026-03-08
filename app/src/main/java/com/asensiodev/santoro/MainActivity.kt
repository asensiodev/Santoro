package com.asensiodev.santoro

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.asensiodev.api.navigation.LoginRoute
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.feature.moviedetail.impl.presentation.navigation.movieDetailRoute
import com.asensiodev.feature.moviedetail.impl.presentation.navigation.navigateToMovieDetail
import com.asensiodev.feature.searchmovies.impl.navigation.seeAllMoviesRoute
import com.asensiodev.login.impl.presentation.navigation.loginScreen
import com.asensiodev.santoro.navigation.DeepLinkHandler
import com.asensiodev.santoro.navigation.SantoroMainTabComponent
import com.asensiodev.santoro.navigation.TabHost
import com.asensiodev.santoro.presentation.onboarding.GuestOnboardingBottomSheet
import com.asensiodev.settings.impl.presentation.navigation.settingsRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    private var pendingDeepLinkMovieId by mutableStateOf(
        DeepLinkHandler.parseMovieIdFromIntent(null),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            pendingDeepLinkMovieId = DeepLinkHandler.parseMovieIdFromIntent(intent)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val themeOption by viewModel.themeOption.collectAsStateWithLifecycle()

            splashScreen.setKeepOnScreenCondition {
                uiState is MainActivityUiState.Loading
            }

            val isDark =
                when (themeOption) {
                    ThemeOption.LIGHT -> false
                    ThemeOption.DARK -> true
                    ThemeOption.SYSTEM -> isSystemInDarkTheme()
                }

            SantoroTheme(darkTheme = isDark) {
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
                            deepLinkMovieId = if (isAuthenticated) pendingDeepLinkMovieId else null,
                            onDeepLinkConsumed = { pendingDeepLinkMovieId = null },
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingDeepLinkMovieId = DeepLinkHandler.parseMovieIdFromIntent(intent)
    }
}

@Composable
@Suppress("FunctionNaming")
fun SantoroApp(
    startDestination: Any,
    isAuthenticated: Boolean,
    deepLinkMovieId: Int? = null,
    onDeepLinkConsumed: () -> Unit = {},
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

    LaunchedEffect(deepLinkMovieId) {
        if (deepLinkMovieId != null) {
            mainNavController.navigateToMovieDetail(deepLinkMovieId)
            onDeepLinkConsumed()
        }
    }

    NavHost(
        navController = mainNavController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(NAV_ANIMATION_DURATION)) },
        exitTransition = { fadeOut(animationSpec = tween(NAV_ANIMATION_DURATION)) },
        popEnterTransition = { fadeIn(animationSpec = tween(NAV_ANIMATION_DURATION)) },
        popExitTransition = { fadeOut(animationSpec = tween(NAV_ANIMATION_DURATION)) },
    ) {
        loginScreen()

        composable<TabHost> {
            SantoroMainTabComponent(mainNavController = mainNavController)
        }

        movieDetailRoute(
            onBackClicked = {
                if (mainNavController.currentBackStackEntry?.lifecycle?.currentState ==
                    Lifecycle.State.RESUMED
                ) {
                    mainNavController.popBackStack()
                }
            },
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(NAV_ANIMATION_DURATION),
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(NAV_ANIMATION_DURATION))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(NAV_ANIMATION_DURATION))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(NAV_ANIMATION_DURATION),
                )
            },
        )

        settingsRoute(
            onBackClicked = {
                if (mainNavController.currentBackStackEntry?.lifecycle?.currentState ==
                    Lifecycle.State.RESUMED
                ) {
                    mainNavController.popBackStack()
                }
            },
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(NAV_ANIMATION_DURATION),
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(NAV_ANIMATION_DURATION))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(NAV_ANIMATION_DURATION))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(NAV_ANIMATION_DURATION),
                )
            },
        )

        seeAllMoviesRoute(
            onMovieClick = { movieId ->
                if (mainNavController.currentBackStackEntry?.lifecycle?.currentState ==
                    Lifecycle.State.RESUMED
                ) {
                    mainNavController.navigateToMovieDetail(movieId)
                }
            },
            onBackClick = {
                if (mainNavController.currentBackStackEntry?.lifecycle?.currentState ==
                    Lifecycle.State.RESUMED
                ) {
                    mainNavController.popBackStack()
                }
            },
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(NAV_ANIMATION_DURATION),
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(NAV_ANIMATION_DURATION))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(NAV_ANIMATION_DURATION))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(NAV_ANIMATION_DURATION),
                )
            },
        )
    }
}

private const val NAV_ANIMATION_DURATION = 300
