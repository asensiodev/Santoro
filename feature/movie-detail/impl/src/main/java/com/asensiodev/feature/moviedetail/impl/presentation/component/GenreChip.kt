package com.asensiodev.feature.moviedetail.impl.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings

@Composable
fun GenreChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            Modifier
                .clip(RoundedCornerShape(Size.size48))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = Spacings.spacing12, vertical = Spacings.spacing4),
    )
}
