package com.asensiodev.feature.watchedmovies.impl.presentation.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.component.querytextfield.QueryTextField
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun WatchedCollectionHeader(
    showsCollectionActions: Boolean,
    totalMovies: Int,
    viewMode: WatchedViewMode,
    isSearchOpen: Boolean,
    query: String,
    onViewModeChange: (WatchedViewMode) -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (showsCollectionActions && isSearchOpen) {
        SearchHeader(
            query = query,
            onCloseSearch = onCloseSearch,
            onQueryChange = onQueryChange,
            modifier = modifier,
        )
    } else {
        TitleHeader(
            showsCollectionActions = showsCollectionActions,
            totalMovies = totalMovies,
            viewMode = viewMode,
            onViewModeChange = onViewModeChange,
            onOpenSearch = onOpenSearch,
            modifier = modifier,
        )
    }
}

@Composable
private fun TitleHeader(
    showsCollectionActions: Boolean,
    totalMovies: Int,
    viewMode: WatchedViewMode,
    onViewModeChange: (WatchedViewMode) -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = Size.size56),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(SR.string.watched_movies),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            if (showsCollectionActions) {
                Text(
                    text =
                        pluralStringResource(
                            SR.plurals.watched_group_movie_count,
                            totalMovies,
                            totalMovies,
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (showsCollectionActions) {
            IconButton(onClick = onOpenSearch) {
                Icon(
                    imageVector = AppIcons.Search,
                    contentDescription =
                        stringResource(
                            SR.string.watched_movies_textfield_placeholder,
                        ),
                )
            }
            WatchedViewModeToggle(
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
            )
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onCloseSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
    BackHandler(onBack = onCloseSearch)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCloseSearch) {
            Icon(
                imageVector = AppIcons.ArrowBack,
                contentDescription = stringResource(SR.string.watched_search_close),
            )
        }
        QueryTextField(
            query = query,
            placeholder = stringResource(SR.string.watched_movies_textfield_placeholder),
            onQueryChanged = onQueryChange,
            modifier =
                Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
        )
    }
}

@PreviewLightDark
@Composable
private fun WatchedCollectionHeaderPreview() {
    PreviewContent {
        WatchedCollectionHeader(
            showsCollectionActions = true,
            totalMovies = PREVIEW_TOTAL_MOVIES,
            viewMode = WatchedViewMode.SHELF,
            isSearchOpen = false,
            query = "",
            onViewModeChange = {},
            onOpenSearch = {},
            onCloseSearch = {},
            onQueryChange = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun WatchedCollectionSearchHeaderPreview() {
    PreviewContent {
        WatchedCollectionHeader(
            showsCollectionActions = true,
            totalMovies = PREVIEW_TOTAL_MOVIES,
            viewMode = WatchedViewMode.SHELF,
            isSearchOpen = true,
            query = "dune",
            onViewModeChange = {},
            onOpenSearch = {},
            onCloseSearch = {},
            onQueryChange = {},
        )
    }
}

private const val PREVIEW_TOTAL_MOVIES = 48
