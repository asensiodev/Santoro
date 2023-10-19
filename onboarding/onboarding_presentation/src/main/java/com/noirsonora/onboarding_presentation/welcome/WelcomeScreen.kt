package com.noirsonora.onboarding_presentation.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.noirsonora.core.navigation.Route
import com.noirsonora.core.util.UiEvent
import com.noirsonora.core_ui.LocalSpacing
import com.noirsonora.onboarding_presentation.components.ActionButton

@Composable
fun WelcomeScreen(
    onNavigate: (UiEvent.Navigate)  -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = com.noirsonora.santoro_core.R.string.welcome_message),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(LocalSpacing.current.spaceMedium))
        ActionButton(
            text = stringResource(id = com.noirsonora.santoro_core.R.string.welcome_button_text),
            onClick = {
                onNavigate(UiEvent.Navigate(Route.LOGIN))
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(onNavigate = {})
}