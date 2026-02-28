package com.asensiodev.feature.watchedmovies.impl.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        ) {
            StatCard(
                icon = AppIcons.Director,
                label = stringResource(SR.string.watched_stat_total_label),
                value = stringResource(SR.string.watched_stat_total_value, stats.totalWatched),
                modifier = Modifier.weight(1f),
            )
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
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        ) {
            StatCard(
                icon = AppIcons.Star,
                label = stringResource(SR.string.watched_stat_genre_label),
                value =
                    stats.favouriteGenre
                        ?: stringResource(SR.string.watched_stat_genre_unavailable),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = AppIcons.Calendar,
                label = stringResource(SR.string.watched_stat_streak_label),
                value =
                    stringResource(
                        SR.string.watched_stat_streak_value,
                        stats.longestStreakWeeks,
                    ),
                modifier = Modifier.weight(1f),
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
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(Spacings.spacing12)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .width(Size.size24)
                        .height(Size.size24),
            )
            Spacer(modifier = Modifier.height(Spacings.spacing8))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacings.spacing4))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
