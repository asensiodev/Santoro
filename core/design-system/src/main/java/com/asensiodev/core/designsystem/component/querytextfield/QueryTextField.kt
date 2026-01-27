package com.asensiodev.core.designsystem.component.querytextfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
fun QueryTextField(
    query: String,
    placeholder: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchTriggered: (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current

    TextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions =
            KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onSearchTriggered?.invoke()
                },
            ),
        leadingIcon = {
            Icon(
                imageVector = AppIcons.SearchIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged(EMPTY_STRING) }) {
                    Icon(
                        imageVector = AppIcons.ClearIcon,
                        contentDescription =
                            stringResource(
                                SR.string.query_text_field_clear_button_description,
                            ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        colors =
            TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor =
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f,
                    ),
            ),
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
