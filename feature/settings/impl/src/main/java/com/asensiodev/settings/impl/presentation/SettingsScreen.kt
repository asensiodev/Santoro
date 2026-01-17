package com.asensiodev.settings.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.component.topbar.SantoroAppBar
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.ui.LaunchEffectOnce
import com.asensiodev.santoro.core.designsystem.R as DR
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

    SettingsScreen(
        onBackClicked = onBackClicked,
        onAppearanceClicked = { /* TODO */ },
        onLanguageClicked = { /* TODO */ },
        onLinkGoogleAccountClicked = {
            viewModel.onSignInWithGoogleClicked(context)
        },
        onLogoutClicked = {
            viewModel.onLogoutClicked()
        },
        isAnonymous = uiState.isAnonymous,
        versionName = versionName ?: "Unknown",
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    onBackClicked: () -> Unit,
    onAppearanceClicked: () -> Unit,
    onLanguageClicked: () -> Unit,
    onLinkGoogleAccountClicked: () -> Unit,
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
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = Size.size16),
        ) {
            SettingsItem(
                text = stringResource(SR.string.settings_appearance),
                icon = AppIcons.SettingsIcon,
                onClick = onAppearanceClicked,
            )
            SettingsItem(
                text = stringResource(SR.string.settings_language),
                icon = AppIcons.InfoIcon,
                onClick = onLanguageClicked,
            )
            if (isAnonymous) {
                SettingsItem(
                    text = stringResource(SR.string.settings_link_google),
                    painter = painterResource(id = DR.drawable.ic_google_logo),
                    onClick = onLinkGoogleAccountClicked,
                )
            } else {
                SettingsItem(
                    text = stringResource(SR.string.settings_logout),
                    icon = AppIcons.ExitToAppIcon,
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

@Composable
fun SettingsItem(
    text: String,
    painter: Painter,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = true,
) {
    SettingsItemContent(
        text = text,
        icon = {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(Size.size24),
            )
        },
        onClick = onClick,
        color = color,
        showChevron = showChevron,
    )
}

@Composable
fun SettingsItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = true,
) {
    SettingsItemContent(
        text = text,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(Size.size24),
            )
        },
        onClick = onClick,
        color = color,
        showChevron = showChevron,
    )
}

@Composable
private fun SettingsItemContent(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    color: Color,
    showChevron: Boolean,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
    ) {
        Column {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Size.size16, vertical = Size.size16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Size.size16),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    icon()
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = color,
                    )
                }

                if (showChevron) {
                    Icon(
                        imageVector = AppIcons.ChevronRightIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
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
        onLinkGoogleAccountClicked = {},
        onLogoutClicked = {},
        isAnonymous = true,
        versionName = "1.0.0",
    )
}
