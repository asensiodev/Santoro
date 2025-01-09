package com.asensiodev.core.designsystem.component.querytextfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
fun QueryTextField(
    query: String,
    placeholder: String,
    onQueryChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Size.size16),
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged(EMPTY_STRING) }) {
                    Icon(
                        imageVector = AppIcons.ClearIcon,
                        contentDescription =
                            stringResource(
                                SR.string.query_text_field_clear_button_description,
                            ),
                    )
                }
            }
        },
    )
}

@PreviewLightDark
@Composable
fun QueryTextFieldEmptyQueryPreview() {
    PreviewContent {
        QueryTextField(
            query = EMPTY_STRING,
            placeholder = "Type to search movies",
            onQueryChanged = {},
        )
    }
}

@PreviewLightDark
@Composable
fun QueryTextFieldActiveQueryPreview() {
    PreviewContent {
        QueryTextField(
            query = "Casino",
            placeholder = "Type to search movies",
            onQueryChanged = {},
        )
    }
}

private const val EMPTY_STRING = ""
