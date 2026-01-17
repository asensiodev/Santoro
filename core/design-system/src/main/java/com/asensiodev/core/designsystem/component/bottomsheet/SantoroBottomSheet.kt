package com.asensiodev.core.designsystem.component.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SantoroBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape =
            RoundedCornerShape(
                topStart = Size.size24,
                topEnd = Size.size24,
            ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = Size.size8,
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun SantoroBottomSheetPreview() {
    PreviewContentFullSize {
        SantoroBottomSheet(
            onDismissRequest = {},
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(Size.size160)
                        .padding(Spacings.spacing16),
            ) {
                Text(text = "This is a bottom sheet content")
            }
        }
    }
}
