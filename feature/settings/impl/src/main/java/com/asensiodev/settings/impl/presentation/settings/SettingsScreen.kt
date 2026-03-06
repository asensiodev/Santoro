package com.asensiodev.settings.impl.presentation.settings

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.topbar.SantoroAppBar
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.settings.impl.presentation.component.LanguagePickerBottomSheet
import com.asensiodev.settings.impl.presentation.component.SettingsItem
import com.asensiodev.settings.impl.presentation.component.ThemePickerBottomSheet
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
    val tmdbUrl = stringResource(SR.string.settings_tmdb_url)
    val privacyPolicyUrl = stringResource(SR.string.settings_privacy_policy_url)

    LaunchedEffect(viewModel) {
        viewModel.process(SettingsIntent.ObserveAuth)
    }

    LaunchedEffect(viewModel) {
        viewModel.process(SettingsIntent.ObserveTheme)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreen(
            onBackClicked = onBackClicked,
            onAppearanceClicked = { viewModel.process(SettingsIntent.OnAppearanceClicked) },
            onLanguageClicked = { viewModel.process(SettingsIntent.OnLanguageClicked) },
            onLogoutClicked = { viewModel.process(SettingsIntent.OnLogoutClicked) },
            onTmdbAttributionClicked = {
                context.startActivity(Intent(Intent.ACTION_VIEW, tmdbUrl.toUri()))
            },
            onPrivacyPolicyClicked = {
                context.startActivity(Intent(Intent.ACTION_VIEW, privacyPolicyUrl.toUri()))
            },
            onDeleteAccountClicked = {
                viewModel.process(SettingsIntent.OnDeleteAccountClicked)
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

        if (uiState.showThemePicker) {
            ThemePickerBottomSheet(
                currentTheme = uiState.currentTheme,
                onThemeSelected = { viewModel.process(SettingsIntent.SetTheme(it)) },
                onDismiss = { viewModel.process(SettingsIntent.DismissThemePicker) },
            )
        }

        if (uiState.showLanguagePicker) {
            LanguagePickerBottomSheet(
                currentLanguage = uiState.currentLanguage,
                onLanguageSelected = { viewModel.process(SettingsIntent.SetLanguage(it)) },
                onDismiss = { viewModel.process(SettingsIntent.DismissLanguagePicker) },
            )
        }

        if (uiState.showDeleteAccountDialog) {
            DeleteAccountConfirmationDialog(
                onConfirm = { viewModel.process(SettingsIntent.ConfirmDeleteAccount) },
                onDismiss = { viewModel.process(SettingsIntent.DismissDeleteAccountDialog) },
            )
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
    onTmdbAttributionClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onDeleteAccountClicked: () -> Unit,
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
            SettingsItem(
                text = stringResource(SR.string.settings_privacy_policy),
                icon = AppIcons.PrivacyPolicy,
                onClick = onPrivacyPolicyClicked,
            )
            if (!isAnonymous) {
                SettingsItem(
                    text = stringResource(SR.string.settings_logout),
                    icon = AppIcons.ExitToApp,
                    onClick = onLogoutClicked,
                    color = MaterialTheme.colorScheme.error,
                    showChevron = false,
                )
                SettingsItem(
                    text = stringResource(SR.string.settings_delete_account),
                    icon = AppIcons.DeleteAccount,
                    onClick = onDeleteAccountClicked,
                    color = MaterialTheme.colorScheme.error,
                    showChevron = false,
                )
            }
            SettingsFooter(
                versionName = versionName,
                onTmdbAttributionClicked = onTmdbAttributionClicked,
            )
        }
    }
}

@Composable
private fun SettingsFooter(
    versionName: String,
    onTmdbAttributionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = Size.size32, bottom = Size.size24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Size.size12),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Size.size4),
        ) {
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
        Row(
            modifier =
                Modifier
                    .clickable(onClick = onTmdbAttributionClicked)
                    .padding(horizontal = Size.size16, vertical = Size.size8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Size.size8),
        ) {
            Text(
                text = stringResource(SR.string.settings_tmdb_attribution),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Image(
                painter = painterResource(DR.drawable.img_tmdb_logo),
                contentDescription = stringResource(SR.string.settings_tmdb_attribution),
                modifier = Modifier.height(Size.size14),
                contentScale = ContentScale.FillHeight,
            )
        }
    }
}

@Composable
private fun DeleteAccountConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(SR.string.settings_delete_account_title))
        },
        text = {
            Text(text = stringResource(SR.string.settings_delete_account_message))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(SR.string.settings_delete_account_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(SR.string.settings_delete_account_cancel))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        onBackClicked = {},
        onAppearanceClicked = {},
        onLanguageClicked = {},
        onLogoutClicked = {},
        onTmdbAttributionClicked = {},
        onPrivacyPolicyClicked = {},
        onDeleteAccountClicked = {},
        isAnonymous = false,
        versionName = "1.0.0",
    )
}
