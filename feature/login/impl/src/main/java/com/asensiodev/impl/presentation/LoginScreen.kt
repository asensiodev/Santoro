package com.asensiodev.impl.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.santoro.core.designsystem.R as DR
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun LoginRoute(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // TODO: Implementa aquí la lógica de Google Sign-In con rememberLauncherForActivityResult
    // y en el resultado, llama a viewModel.signInWithCredential(credential)

    LoginScreen(
        uiState = uiState,
        onAnonymousLoginClicked = viewModel::signInAnonymously,
        onGoogleLoginClicked = { /* TODO: Lanza aquí el flujo de Google Sign-In */ },
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
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Spacings.spacing24),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
            } else {
                LoginContent(
                    onAnonymousLoginClicked = onAnonymousLoginClicked,
                    onGoogleLoginClicked = onGoogleLoginClicked,
                )
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = Spacings.spacing16),
                )
            }
        }
    }
}

@Composable
internal fun LoginContent(
    onAnonymousLoginClicked: () -> Unit,
    onGoogleLoginClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = DR.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier =
                Modifier
                    .clip(
                        RoundedCornerShape(size = Size.size120),
                    ).background(MaterialTheme.colorScheme.primaryContainer)
                    .size(Size.size136),
        )
        Spacer(modifier = Modifier.height(Spacings.spacing64))
        Button(
            onClick = onAnonymousLoginClicked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = AppIcons.UserIcon,
                contentDescription = null,
                modifier = Modifier.size(Size.size24),
            )
            Spacer(modifier = Modifier.width(Spacings.spacing12))
            Text(stringResource(SR.string.login_anonymous_login_button))
        }
        Spacer(modifier = Modifier.height(Spacings.spacing16))
        Button(
            onClick = onGoogleLoginClicked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(id = DR.drawable.ic_google_logo),
                contentDescription = null,
                modifier = Modifier.size(Size.size24),
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.width(Spacings.spacing12))
            Text(stringResource(SR.string.login_google_login_button))
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
