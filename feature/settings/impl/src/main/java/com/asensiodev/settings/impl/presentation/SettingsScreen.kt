package com.asensiodev.settings.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.asensiodev.core.designsystem.component.banner.Banner
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.topbar.SantoroAppBar
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.settings.impl.presentation.component.AccountLinkedSuccessBottomSheet
import com.asensiodev.ui.LaunchEffectOnce
import com.asensiodev.ui.UiText
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

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreen(
            onBackClicked = onBackClicked,
            onAppearanceClicked = { /* TODO */ },
            onLanguageClicked = { /* TODO */ },
            onLinkGoogleAccountClicked = {
                viewModel.onSignInWithGoogleClicked(context)
            },
            onRetryError = { viewModel.onSignInWithGoogleClicked(context) },
            onLogoutClicked = {
                viewModel.onLogoutClicked()
            },
            onLinkAccountSuccessDismiss = viewModel::onLinkAccountSuccessDismiss,
            onAccountCollisionDialogDismiss = viewModel::onAccountCollisionDialogDismiss,
            onAccountCollisionDialogConfirm = viewModel::onAccountCollisionDialogConfirm,
            isAnonymous = uiState.isAnonymous,
            versionName = versionName ?: "Unknown",
            error = uiState.error,
            isLinkAccountSuccessful = uiState.isLinkAccountSuccessful,
            showAccountCollisionDialog = uiState.showAccountCollisionDialog,
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
    onLinkGoogleAccountClicked: () -> Unit,
    onRetryError: () -> Unit,
    onLogoutClicked: () -> Unit,
    onLinkAccountSuccessDismiss: () -> Unit,
    onAccountCollisionDialogDismiss: () -> Unit,
    onAccountCollisionDialogConfirm: () -> Unit,
    isAnonymous: Boolean,
    versionName: String,
    error: UiText?,
    isLinkAccountSuccessful: Boolean,
    showAccountCollisionDialog: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isLinkAccountSuccessful) {
        AccountLinkedSuccessBottomSheet(
            onDismiss = onLinkAccountSuccessDismiss,
        )
    }
    if (showAccountCollisionDialog) {
        AccountAlreadyExistsDialog(onAccountCollisionDialogDismiss, onAccountCollisionDialogConfirm)
    }
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
            if (error != null) {
                ErrorBanner(error, onRetryError)
            }
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
            if (isAnonymous) {
                SettingsItem(
                    text = stringResource(SR.string.settings_link_google),
                    painter = painterResource(id = DR.drawable.ic_google_logo),
                    onClick = onLinkGoogleAccountClicked,
                )
            } else {
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
private fun ErrorBanner(
    error: UiText,
    onRetryError: () -> Unit,
) {
    Banner(
        message = error.asString(),
        onRetry = onRetryError,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Size.size16, vertical = Size.size8),
    )
}

@Composable
private fun AccountAlreadyExistsDialog(
    onAccountCollisionDialogDismiss: () -> Unit,
    onAccountCollisionDialogConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onAccountCollisionDialogDismiss,
        title = {
            Text(text = stringResource(SR.string.settings_account_collision_title))
        },
        text = {
            Text(text = stringResource(SR.string.settings_account_collision_message))
        },
        confirmButton = {
            TextButton(
                onClick = onAccountCollisionDialogConfirm,
            ) {
                Text(text = stringResource(SR.string.settings_account_collision_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onAccountCollisionDialogDismiss,
            ) {
                Text(text = stringResource(SR.string.settings_account_collision_cancel))
            }
        },
    )
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
                        imageVector = AppIcons.ChevronRight,
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
        onRetryError = {},
        onLogoutClicked = {},
        isAnonymous = true,
        versionName = "1.0.0",
        error = null,
        isLinkAccountSuccessful = false,
        onLinkAccountSuccessDismiss = {},
        onAccountCollisionDialogDismiss = {},
        onAccountCollisionDialogConfirm = {},
        showAccountCollisionDialog = false,
    )
}
