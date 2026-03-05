package com.asensiodev.feature.watchedmovies.impl.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchedmovies.impl.domain.model.WatchedStats
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun WatchedStatsDashboard(
    stats: WatchedStats,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
    ) {
        item {
            StatCard(
                icon = AppIcons.Director,
                label = stringResource(SR.string.watched_stat_total_label),
                value = stringResource(SR.string.watched_stat_total_value, stats.totalWatched),
            )
        }
        item {
            StatCard(
                icon = AppIcons.Duration,
                label = stringResource(SR.string.watched_stat_runtime_label),
                value =
                    if (stats.totalRuntimeHours > 0) {
                        stringResource(
                            SR.string.watched_stat_runtime_value,
                            stats.totalRuntimeHours,
                        )
                    } else {
                        stringResource(SR.string.watched_stat_runtime_unavailable)
                    },
            )
        }
        item {
            StatCard(
                icon = AppIcons.Star,
                label = stringResource(SR.string.watched_stat_genre_label),
                value =
                    stats.favouriteGenre
                        ?: stringResource(SR.string.watched_stat_genre_unavailable),
            )
        }
        item {
            StatCard(
                icon = AppIcons.Calendar,
                label = stringResource(SR.string.watched_stat_streak_label),
                value =
                    stringResource(
                        SR.string.watched_stat_streak_value,
                        stats.longestStreakWeeks,
                    ),
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Row(
            modifier = Modifier.padding(Spacings.spacing12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(Size.size24),
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
