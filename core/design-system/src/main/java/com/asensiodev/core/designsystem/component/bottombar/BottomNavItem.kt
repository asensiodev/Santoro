package com.asensiodev.core.designsystem.component.bottombar

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val labelRes: Int,
    val isSelected: Boolean,
    val onClick: () -> Unit,
)
