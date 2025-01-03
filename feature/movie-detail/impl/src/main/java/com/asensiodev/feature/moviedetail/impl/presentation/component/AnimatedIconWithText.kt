package com.asensiodev.feature.moviedetail.impl.presentation.component

import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings

@Composable
internal fun AnimatedIconWithText(
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing4),
        modifier = modifier,
    ) {
        val transition = updateTransition(targetState = isSelected, label = "Icon Transition")
        val icon by transition.animateInt(
            label = "Icon Animation",
        ) { if (it) SELECTED_ICON else UNSELECTED_ICON }

        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (icon == SELECTED_ICON) selectedIcon else unselectedIcon,
                contentDescription = null,
                modifier = Modifier.size(Size.size40),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = SINGLE_LINE,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private const val SELECTED_ICON = 1
private const val UNSELECTED_ICON = 2
private const val SINGLE_LINE = 1
