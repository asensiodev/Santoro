package com.asensiodev.feature.searchmovies.impl.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.santoro.core.stringresources.R as SR

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SearchSuggestionsContent(
    recentSearches: List<String>,
    trendingSuggestions: List<String>,
    onSuggestionTap: (String) -> Unit,
    onClearRecents: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
    ) {
        if (recentSearches.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(SR.string.search_suggestions_recent_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onClearRecents) {
                        Text(
                            text = stringResource(SR.string.search_suggestions_clear_all),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
                ) {
                    recentSearches.forEach { query ->
                        SuggestionChip(
                            onClick = { onSuggestionTap(query) },
                            label = { Text(text = query) },
                        )
                    }
                }
            }
        }

        if (trendingSuggestions.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(SR.string.search_suggestions_trending_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacings.spacing8),
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
                ) {
                    items(trendingSuggestions) { title ->
                        SuggestionChip(
                            onClick = { onSuggestionTap(title) },
                            label = { Text(text = title) },
                        )
                    }
                }
            }
        }
    }
}
