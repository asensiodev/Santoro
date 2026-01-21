package com.asensiodev.settings.impl.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.santoro.core.stringresources.R as SR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountLinkedSuccessBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacings.spacing24)
                    .padding(bottom = Spacings.spacing48),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = AppIcons.WatchedMoviesIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Size.size64),
            )
            Spacer(modifier = Modifier.height(Spacings.spacing16))
            Text(
                text = stringResource(SR.string.settings_account_linked_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(Spacings.spacing8))
            Text(
                text = stringResource(SR.string.settings_account_linked_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacings.spacing32))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(SR.string.settings_account_linked_button))
            }
        }
    }
}
