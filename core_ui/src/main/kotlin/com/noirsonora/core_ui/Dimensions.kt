package com.noirsonora.core_ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    val zero: Dp = 0.dp,
    val default: Dp = 2.dp,
    val spaceExtraSmall: Dp = 4.dp,
    val spaceSmall: Dp = 8.dp,
    val spaceMedium: Dp = 16.dp,
    val spaceLarge: Dp = 32.dp,
    val spaceExtraLarge: Dp = 40.dp,

    // Size
    val sizeExtraSmall: Dp = 4.dp,
    val sizeSmall: Dp = 8.dp,
    val sizeMedium: Dp = 16.dp,
    val sizeLarge: Dp = 32.dp,
    val sizeExtraLarge: Dp = 48.dp,
    val sizeOneHundred: Dp = 100.dp

)
val LocalDimensions = compositionLocalOf { Dimensions() }

