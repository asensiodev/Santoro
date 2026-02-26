package com.asensiodev.settings.impl.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.santoro.core.stringresources.R as SR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThemePickerBottomSheet(
    currentTheme: ThemeOption,
    onThemeSelected: (ThemeOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacings.spacing24),
        ) {
            Text(
                text = stringResource(SR.string.settings_appearance_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier.padding(
                        horizontal = Spacings.spacing16,
                        vertical = Spacings.spacing12,
                    ),
            )

            ThemeOptionRow(
                label = stringResource(SR.string.settings_appearance_system),
                selected = currentTheme == ThemeOption.SYSTEM,
                onClick = { onThemeSelected(ThemeOption.SYSTEM) },
            )
            ThemeOptionRow(
                label = stringResource(SR.string.settings_appearance_light),
                selected = currentTheme == ThemeOption.LIGHT,
                onClick = { onThemeSelected(ThemeOption.LIGHT) },
            )
            ThemeOptionRow(
                label = stringResource(SR.string.settings_appearance_dark),
                selected = currentTheme == ThemeOption.DARK,
                onClick = { onThemeSelected(ThemeOption.DARK) },
            )
        }
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Spacings.spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
