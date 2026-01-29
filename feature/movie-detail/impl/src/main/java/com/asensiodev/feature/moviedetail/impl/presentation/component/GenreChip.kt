package com.asensiodev.feature.moviedetail.impl.presentation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings

@Composable
fun GenreChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant, // Lighter text color
        modifier =
            modifier
                .border(
                    width = Size.size1,
                    color = MaterialTheme.colorScheme.outlineVariant, // Outline style
                    shape = RoundedCornerShape(Size.size48),
                ).padding(horizontal = Spacings.spacing12, vertical = Spacings.spacing4),
    )
}

@PreviewLightDark
@Composable
private fun GenreChipPreview() {
    PreviewContent {
        GenreChip(text = "Action")
    }
}
