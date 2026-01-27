package com.asensiodev.login.impl.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.banner.Banner
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.designsystem.theme.displayFontFamily
import com.asensiodev.feature.login.impl.R
import com.asensiodev.ui.UiText
import com.asensiodev.santoro.core.designsystem.R as DR
import com.asensiodev.santoro.core.stringresources.R as SR

private const val BACKGROUND_GRADIENT_START_ALPHA = 0.6f
private const val BACKGROUND_GRADIENT_END_ALPHA = 0.95f

@Composable
internal fun LoginRoute(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LoginScreen(
        uiState = uiState,
        onAnonymousLoginClicked = viewModel::signInAnonymously,
        onGoogleLoginClicked = {
            viewModel.onSignInWithGoogleClicked(context)
        },
        modifier = modifier,
    )
}

@Composable
internal fun LoginScreen(
    uiState: LoginUiState,
    onAnonymousLoginClicked: () -> Unit,
    onGoogleLoginClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_login_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(BACKGROUND_GRADIENT_START_ALPHA),
                                        Color.Black.copy(BACKGROUND_GRADIENT_END_ALPHA),
                                    ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY,
                            ),
                        ),
            )
            LoginContent(paddingValues, uiState, onGoogleLoginClicked, onAnonymousLoginClicked)
            LoadingContent(uiState)
        }
    }
}

@Composable
private fun LoginContent(
    paddingValues: PaddingValues,
    uiState: LoginUiState,
    onGoogleLoginClicked: () -> Unit,
    onAnonymousLoginClicked: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacings.spacing24)
                .padding(bottom = Spacings.spacing48),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                painter = painterResource(id = DR.drawable.ic_launcher_foreground),
                contentDescription = stringResource(SR.string.app_name),
                tint = Color.White,
                modifier =
                    Modifier
                        .size(Size.size120)
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape,
                        ).padding(Spacings.spacing24),
            )
            Spacer(modifier = Modifier.height(Spacings.spacing24))
            Text(
                text = stringResource(SR.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                fontFamily = displayFontFamily,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(Spacings.spacing8))
            Text(
                text = stringResource(SR.string.login_slogan),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }
        if (uiState.errorMessage != null) {
            Banner(
                message = uiState.errorMessage.asString(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(Spacings.spacing24))
        }
        LoginButtonsSection(onGoogleLoginClicked, onAnonymousLoginClicked)
    }
}

@Composable
private fun LoginButtonsSection(
    onGoogleLoginClicked: () -> Unit,
    onAnonymousLoginClicked: () -> Unit,
) {
    Button(
        onClick = onGoogleLoginClicked,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(Size.size56),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
            ),
        shape = CircleShape,
    ) {
        Icon(
            painter = painterResource(id = DR.drawable.ic_google_logo),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(Size.size24),
        )
        Spacer(modifier = Modifier.width(Spacings.spacing12))
        Text(
            text = stringResource(SR.string.login_google_login_button),
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
        )
    }
    Spacer(modifier = Modifier.height(Spacings.spacing16))
    Button(
        onClick = onAnonymousLoginClicked,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(Size.size56),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
            ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
        shape = CircleShape,
    ) {
        Icon(
            imageVector = AppIcons.UserIcon,
            contentDescription = null,
            modifier = Modifier.size(Size.size24),
        )
        Spacer(modifier = Modifier.width(Spacings.spacing12))
        Text(
            text = stringResource(SR.string.login_anonymous_login_button),
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
        )
    }
}

@Composable
private fun LoadingContent(uiState: LoginUiState) {
    if (uiState.isLoading) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) {},
            contentAlignment = Alignment.Center,
        ) {
            LoadingIndicator()
        }
    }
}

@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
    PreviewContentFullSize {
        LoginScreen(
            uiState = LoginUiState(isLoading = false, errorMessage = null),
            onAnonymousLoginClicked = {},
            onGoogleLoginClicked = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun LoginScreenErrorPreview() {
    PreviewContentFullSize {
        LoginScreen(
            uiState =
                LoginUiState(
                    isLoading = false,
                    errorMessage =
                        UiText.DynamicString(
                            "Ups! An error occured. Please try again later",
                        ),
                ),
            onAnonymousLoginClicked = {},
            onGoogleLoginClicked = {},
        )
    }
}
