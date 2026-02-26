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
import com.asensiodev.core.domain.model.AppLanguage
import com.asensiodev.santoro.core.stringresources.R as SR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LanguagePickerBottomSheet(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
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
                text = stringResource(SR.string.settings_language_picker_title),
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier.padding(
                        horizontal = Spacings.spacing16,
                        vertical = Spacings.spacing12,
                    ),
            )

            LanguageOptionRow(
                label = stringResource(SR.string.settings_language_english),
                selected = currentLanguage == AppLanguage.ENGLISH,
                onClick = { onLanguageSelected(AppLanguage.ENGLISH) },
            )
            LanguageOptionRow(
                label = stringResource(SR.string.settings_language_spanish),
                selected = currentLanguage == AppLanguage.SPANISH,
                onClick = { onLanguageSelected(AppLanguage.SPANISH) },
            )
        }
    }
}

@Composable
private fun LanguageOptionRow(
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
