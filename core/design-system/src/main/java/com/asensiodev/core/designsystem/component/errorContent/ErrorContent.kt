package com.asensiodev.core.designsystem.component.errorContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacings.spacing16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = Spacings.spacing16),
        )
        Button(onClick = onRetry) {
            Text(stringResource(SR.string.search_movies_error_button_retry))
        }
    }
}

@Preview
@Composable
private fun ErrorContentPreview() {
    PreviewContent {
        ErrorContent(
            message = "An error occurred",
            onRetry = {},
        )
    }
}
