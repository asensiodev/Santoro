package com.asensiodev.core.designsystem.component.bottombar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        tonalElevation = Size.size0,
    ) {
        items.forEach { item ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.75f else 1.0f,
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                label = "scale",
            )

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = item.onClick,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.scale(scale),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Crossfade(
                        targetState = item.isSelected,
                        label = "iconCrossfade",
                        animationSpec = tween(durationMillis = 200),
                    ) { isSelected ->
                        Icon(
                            imageVector =
                                if (isSelected) {
                                    item.selectedIcon
                                } else {
                                    item.unselectedIcon
                                },
                            contentDescription = null,
                            tint =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }

                    Spacer(modifier = Modifier.height(Size.size4))

                    Text(
                        text = stringResource(item.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Medium,
                        color =
                            if (item.isSelected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(
        items =
            listOf(
                BottomNavItem(
                    selectedIcon = AppIcons.Home,
                    unselectedIcon = AppIcons.HomeOutlined,
                    labelRes = SR.string.search_movies,
                    isSelected = true,
                    onClick = {},
                ),
                BottomNavItem(
                    selectedIcon = AppIcons.Watched,
                    unselectedIcon = AppIcons.WatchedOutlined,
                    labelRes = SR.string.watched_movies,
                    isSelected = false,
                    onClick = {},
                ),
                BottomNavItem(
                    selectedIcon = AppIcons.Watchlist,
                    unselectedIcon = AppIcons.WatchlistOutlined,
                    labelRes = SR.string.watchlist,
                    isSelected = false,
                    onClick = {},
                ),
            ),
    )
}
