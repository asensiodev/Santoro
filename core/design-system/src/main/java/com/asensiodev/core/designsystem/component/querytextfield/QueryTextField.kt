package com.asensiodev.core.designsystem.component.querytextfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
fun QueryTextField(
    query: String,
    placeholder: String,
    onQueryChanged: (String) -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
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

private const val EMPTY_STRING = ""
