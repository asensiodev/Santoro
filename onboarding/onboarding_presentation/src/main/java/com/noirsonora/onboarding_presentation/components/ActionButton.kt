package com.noirsonora.onboarding_presentation.components


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noirsonora.core_ui.LocalSpacing

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = isEnabled,
        shape = RoundedCornerShape(100.dp),
        content = {
            Text(
                text = text,
                style = textStyle,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(LocalSpacing.current.spaceExtraSmall)
            )
        }
    )

}

@Preview
@Composable
fun ActionButtonPreview() {
    ActionButton(
        text = "Button",
        onClick = {},
        isEnabled = true,
        //modifier = Modifier.align(),
        textStyle = MaterialTheme.typography.bodyMedium
    )

}