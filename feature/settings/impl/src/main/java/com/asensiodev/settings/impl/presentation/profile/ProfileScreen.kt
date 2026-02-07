package com.asensiodev.settings.impl.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.asensiodev.core.designsystem.component.banner.Banner
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.settings.impl.presentation.component.AccountLinkedSuccessBottomSheet
import com.asensiodev.settings.impl.presentation.component.SettingsItem
import com.asensiodev.ui.LaunchEffectOnce
import com.asensiodev.ui.UiText
import java.util.Locale
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun ProfileScreenRoute(
    onAppSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchEffectOnce {
        viewModel.observeAuthState()
    }

    Box(modifier = modifier.fillMaxSize()) {
        ProfileScreen(
            onAppSettingsClicked = onAppSettingsClicked,
            onHelpClicked = { /* TODO */ },
            onLinkGoogleAccountClicked = {
                viewModel.onSignInWithGoogleClicked(context)
            },
            onRetryError = { viewModel.onSignInWithGoogleClicked(context) },
            onLinkAccountSuccessDismiss = viewModel::onLinkAccountSuccessDismiss,
            onAccountCollisionDialogDismiss = viewModel::onAccountCollisionDialogDismiss,
            onAccountCollisionDialogConfirm = viewModel::onAccountCollisionDialogConfirm,
            uiState = uiState,
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

@Composable
internal fun ProfileScreen(
    onAppSettingsClicked: () -> Unit,
    onHelpClicked: () -> Unit,
    onLinkGoogleAccountClicked: () -> Unit,
    onRetryError: () -> Unit,
    onLinkAccountSuccessDismiss: () -> Unit,
    onAccountCollisionDialogDismiss: () -> Unit,
    onAccountCollisionDialogConfirm: () -> Unit,
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLinkAccountSuccessful) {
        AccountLinkedSuccessBottomSheet(
            onDismiss = onLinkAccountSuccessDismiss,
        )
    }
    if (uiState.showAccountCollisionDialog) {
        AccountAlreadyExistsDialog(
            onAccountCollisionDialogDismiss,
            onAccountCollisionDialogConfirm,
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = Size.size24),
    ) {
        Spacer(modifier = Modifier.height(Size.size48))
        UserHeader(
            user = uiState.user,
            isAnonymous = uiState.isAnonymous,
        )
        Spacer(modifier = Modifier.height(Size.size32))

        if (uiState.error != null) {
            ErrorBanner(uiState.error, onRetryError)
        }

        if (uiState.isAnonymous) {
            GrowthCard(
                onClick = onLinkGoogleAccountClicked,
                modifier =
                    Modifier
                        .padding(horizontal = Size.size16)
                        .padding(bottom = Size.size24),
            )
        }

        SettingsItem(
            text = stringResource(SR.string.profile_app_settings),
            icon = AppIcons.Settings,
            onClick = onAppSettingsClicked,
        )
        SettingsItem(
            text = stringResource(SR.string.profile_help_legal),
            icon = AppIcons.Help,
            onClick = onHelpClicked,
        )
    }
}

private const val MAIL_SUBSTRING_CHAR = "@"

@Composable
private fun UserHeader(
    user: SantoroUser?,
    isAnonymous: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Size.size16),
    ) {
        val photoUrl = user?.photoUrl
        if (!isAnonymous && !photoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier =
                    Modifier
                        .size(Size.size80)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(AppIcons.Profile),
                placeholder = rememberVectorPainter(AppIcons.Profile),
            )
        } else {
            Icon(
                imageVector = AppIcons.Profile,
                contentDescription = null,
                modifier =
                    Modifier
                        .size(Size.size80)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(Size.size16),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val displayName =
            remember(user, isAnonymous) {
                if (isAnonymous) return@remember null

                val realName = user?.displayName
                if (!realName.isNullOrBlank()) return@remember realName

                user?.email?.substringBefore(MAIL_SUBSTRING_CHAR)?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }

        val greetingName = displayName ?: stringResource(SR.string.profile_guest_name)

        Text(
            text = stringResource(SR.string.profile_user_greeting, greetingName),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun GrowthCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(Size.size16),
            verticalArrangement = Arrangement.spacedBy(Size.size12),
        ) {
            Text(
                text = stringResource(SR.string.profile_growth_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(SR.string.profile_growth_card_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(text = stringResource(SR.string.profile_growth_card_button))
            }
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
