package com.asensiodev.settings.impl.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.topbar.SantoroAppBar
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.settings.impl.presentation.component.SettingsItem
import com.asensiodev.ui.LaunchEffectOnce
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun SettingsScreenRoute(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val versionName =
        remember(context) {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionName
            } catch (_: Exception) {
                "Unknown"
            }
        }

    LaunchEffectOnce {
        viewModel.observeAuthState()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreen(
            onBackClicked = onBackClicked,
            onAppearanceClicked = { /* TODO */ },
            onLanguageClicked = { /* TODO */ },
            onLogoutClicked = {
                viewModel.onLogoutClicked()
            },
            isAnonymous = uiState.isAnonymous,
            versionName = versionName ?: "Unknown",
            modifier = modifier,
        )

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    onBackClicked: () -> Unit,
    onAppearanceClicked: () -> Unit,
    onLanguageClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    isAnonymous: Boolean,
    versionName: String,
    modifier: Modifier = Modifier,
) {
    SantoroAppBar(
        title = stringResource(SR.string.settings_title),
        onBackClicked = onBackClicked,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
        ) {
            SettingsItem(
                text = stringResource(SR.string.settings_appearance),
                icon = AppIcons.Profile,
                onClick = onAppearanceClicked,
            )
            SettingsItem(
                text = stringResource(SR.string.settings_language),
                icon = AppIcons.Info,
                onClick = onLanguageClicked,
            )
            if (!isAnonymous) {
                SettingsItem(
                    text = stringResource(SR.string.settings_logout),
                    icon = AppIcons.ExitToApp,
                    onClick = onLogoutClicked,
                    color = MaterialTheme.colorScheme.error,
                    showChevron = false,
                )
            }
            Spacer(modifier = Modifier.height(Size.size32))
            VersionSection(versionName)
        }
    }
}

@Composable
private fun VersionSection(versionName: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = Size.size24),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(SR.string.settings_version, versionName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(SR.string.settings_author),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        onBackClicked = {},
        onAppearanceClicked = {},
        onLanguageClicked = {},
        onLogoutClicked = {},
        isAnonymous = true,
        versionName = "1.0.0",
    )
}
