package com.asensiodev.santoro.presentation.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.bottomsheet.SantoroBottomSheet
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.santoro.core.stringresources.R as SR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestOnboardingBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SantoroBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { it != SheetValue.Hidden },
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = Spacings.spacing24)
                    .padding(bottom = Spacings.spacing24),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
//            Image(
//                painter = painterResource(id = DR.drawable.ic_launcher_foreground),
//                contentDescription = null,
//                modifier =
//                    Modifier
//                        .size(Size.size80)
//                        .padding(bottom = Spacings.spacing16),
//            )

            Text(
                text = stringResource(id = SR.string.guest_onboarding_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(Spacings.spacing16))

            Text(
                text = stringResource(id = SR.string.guest_onboarding_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacings.spacing32))

            Button(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(id = SR.string.guest_onboarding_button))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GuestOnboardingBottomSheetPreview() {
    PreviewContentFullSize {
        GuestOnboardingBottomSheet(
            onDismissRequest = {},
        )
    }
}
