package com.asensiodev.feature.watchedmovies.impl.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun WatchedGroupingChips(
    grouping: WatchedGrouping,
    onGroupingChange: (WatchedGrouping) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
    ) {
        WatchedGrouping.entries.forEach { option ->
            val selected = option == grouping
            val bringIntoViewRequester = remember { BringIntoViewRequester() }
            LaunchedEffect(selected) {
                if (selected) {
                    withFrameNanos {}
                    bringIntoViewRequester.bringIntoView()
                }
            }
            FilterChip(
                selected = selected,
                onClick = { onGroupingChange(option) },
                label = { Text(text = option.label()) },
                leadingIcon =
                    if (selected) {
                        {
                            Icon(
                                imageVector = AppIcons.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                shape = RoundedCornerShape(Size.size16),
                modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
            )
        }
    }
}

@Composable
internal fun WatchedViewModeToggle(
    viewMode: WatchedViewMode,
    onViewModeChange: (WatchedViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        WatchedViewMode.entries.forEachIndexed { index, option ->
            val description = option.label()
            SegmentedButton(
                selected = option == viewMode,
                onClick = { onViewModeChange(option) },
                shape = SegmentedButtonDefaults.itemShape(index, WatchedViewMode.entries.size),
                icon = {},
                modifier =
                    Modifier
                        .heightIn(min = Size.size48)
                        .semantics { contentDescription = description },
            ) {
                Icon(imageVector = option.icon(), contentDescription = null)
            }
        }
    }
}

@Composable
private fun WatchedGrouping.label(): String =
    stringResource(
        when (this) {
            WatchedGrouping.DATE_WATCHED -> SR.string.watched_group_date
            WatchedGrouping.DIRECTOR -> SR.string.watched_group_director
            WatchedGrouping.GENRE -> SR.string.watched_group_genre
            WatchedGrouping.DECADE -> SR.string.watched_group_decade
        },
    )

@Composable
private fun WatchedViewMode.label(): String =
    stringResource(
        when (this) {
            WatchedViewMode.SHELF -> SR.string.watched_view_mode_shelf
            WatchedViewMode.LIST -> SR.string.watched_view_mode_list
        },
    )

private fun WatchedViewMode.icon(): ImageVector =
    when (this) {
        WatchedViewMode.SHELF -> AppIcons.GridView
        WatchedViewMode.LIST -> AppIcons.ViewList
    }

@PreviewLightDark
@Composable
private fun WatchedGroupingChipsPreview() {
    PreviewContent {
        WatchedGroupingChips(
            grouping = WatchedGrouping.DIRECTOR,
            onGroupingChange = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun WatchedViewModeTogglePreview() {
    PreviewContent {
        WatchedViewModeToggle(
            viewMode = WatchedViewMode.SHELF,
            onViewModeChange = {},
        )
    }
}
