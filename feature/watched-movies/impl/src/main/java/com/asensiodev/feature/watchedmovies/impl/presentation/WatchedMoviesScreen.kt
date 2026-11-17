package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.noresultscontent.NoResultsContent
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.feature.watchedmovies.impl.presentation.component.MovieCard
import com.asensiodev.feature.watchedmovies.impl.presentation.component.WatchedCollectionHeader
import com.asensiodev.feature.watchedmovies.impl.presentation.component.WatchedGroupingChips
import com.asensiodev.feature.watchedmovies.impl.presentation.component.WatchedMovieRow
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupLabel
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupUi
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun WatchedMoviesRoute(
    onMovieClick: (Int) -> Unit,
    onDirectorClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchedMoviesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.process(WatchedMoviesIntent.LoadMovies)
    }

    val onProcess = remember(viewModel) { viewModel::process }

    WatchedMoviesScreen(
        uiState = uiState,
        onProcess = onProcess,
        onMovieClick = onMovieClick,
        onDirectorClick = onDirectorClick,
        modifier = modifier,
    )
}

@Composable
internal fun WatchedMoviesScreen(
    uiState: WatchedMoviesUiState,
    onProcess: (WatchedMoviesIntent) -> Unit,
    onMovieClick: (Int) -> Unit,
    onDirectorClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    var searchWasOpen by remember { mutableStateOf(uiState.isSearchOpen) }
    LaunchedEffect(uiState.isSearchOpen) {
        if (searchWasOpen && !uiState.isSearchOpen) gridState.scrollToItem(0)
        searchWasOpen = uiState.isSearchOpen
    }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = Spacings.spacing16)
                .padding(top = Spacings.spacing16),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        val showsCollectionControls = uiState.showsCollectionControls()
        WatchedCollectionHeader(
            showsCollectionActions = showsCollectionControls,
            totalMovies = uiState.totalMovies,
            viewMode = uiState.viewMode,
            isSearchOpen = uiState.isSearchOpen,
            query = uiState.query,
            onViewModeChange = { onProcess(WatchedMoviesIntent.ChangeViewMode(it)) },
            onOpenSearch = { onProcess(WatchedMoviesIntent.OpenSearch) },
            onCloseSearch = { onProcess(WatchedMoviesIntent.CloseSearch) },
            onQueryChange = { onProcess(WatchedMoviesIntent.UpdateQuery(it)) },
        )
        if (showsCollectionControls) {
            WatchedGroupingChips(
                grouping = uiState.grouping,
                onGroupingChange = { onProcess(WatchedMoviesIntent.ChangeGrouping(it)) },
            )
        }
        when (uiState.screenState) {
            is WatchedScreenState.Loading -> {
                LoadingIndicator()
            }

            is WatchedScreenState.Error -> {
                ErrorContent(
                    message = stringResource(SR.string.error_message_retry),
                    onRetry = { onProcess(WatchedMoviesIntent.LoadMovies) },
                )
            }

            is WatchedScreenState.Content -> {
                WatchedMovieList(
                    groups = uiState.groups,
                    viewMode = uiState.viewMode,
                    gridState = gridState,
                    onMovieClick = onMovieClick,
                    onDirectorClick = onDirectorClick,
                    onToggleGroup = { onProcess(WatchedMoviesIntent.ToggleGroup(it)) },
                )
            }

            is WatchedScreenState.NoResults -> {
                NoResultsContent(
                    text = stringResource(SR.string.watched_movies_search_no_results_text),
                    subtitle = stringResource(SR.string.watched_movies_search_no_results_subtitle),
                )
            }

            is WatchedScreenState.Empty -> {
                NoResultsContent(
                    text = stringResource(SR.string.watched_movies_no_results_text),
                    subtitle = stringResource(SR.string.watched_movies_no_results_subtitle),
                    icon = AppIcons.WatchedOutlined,
                )
            }
        }
    }
}

@Composable
internal fun WatchedMovieList(
    groups: List<WatchedGroupUi>,
    viewMode: WatchedViewMode,
    onMovieClick: (Int) -> Unit,
    onDirectorClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    onToggleGroup: (String) -> Unit = {},
    gridState: LazyGridState = rememberLazyGridState(),
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(SHELF_COLUMNS),
        state = gridState,
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        contentPadding = PaddingValues(bottom = Spacings.spacing16),
        modifier = modifier.fillMaxSize(),
    ) {
        groups.forEach { group ->
            val label = group.label
            if (label != null) {
                item(
                    key = group.key,
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = CONTENT_TYPE_HEADER,
                ) {
                    WatchedGroupHeading(
                        group = group,
                        label = label,
                        showsExpandControl = group.isCollapsible,
                        onDirectorClick = onDirectorClick,
                        onToggleGroup = onToggleGroup,
                    )
                }
            }
            if (!group.isExpanded) return@forEach
            when (viewMode) {
                WatchedViewMode.SHELF ->
                    items(
                        items = group.movies,
                        key = { "${group.key}$KEY_SEPARATOR${it.id}" },
                        contentType = { CONTENT_TYPE_MOVIE },
                    ) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) },
                            modifier = Modifier.aspectRatio(POSTER_RATIO),
                        )
                    }

                WatchedViewMode.LIST ->
                    items(
                        items = group.movies,
                        key = { "${group.key}$KEY_SEPARATOR${it.id}" },
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = { CONTENT_TYPE_ROW },
                    ) { movie ->
                        WatchedMovieRow(movie = movie, onClick = { onMovieClick(movie.id) })
                    }
            }
        }
    }
}

@Composable
private fun WatchedGroupHeading(
    group: WatchedGroupUi,
    label: WatchedGroupLabel,
    showsExpandControl: Boolean,
    onDirectorClick: (Int, String) -> Unit,
    onToggleGroup: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val directorId = group.directorId
    val title = label.asTitle()
    val isDirectorHeading = group.isCollapsible || directorId != null
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = Spacings.spacing4)
                .then(
                    if (isDirectorHeading) {
                        Modifier
                            .heightIn(min = Size.size56)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    } else {
                        Modifier.heightIn(min = Size.size48)
                    },
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showsExpandControl) {
            ExpandToggle(
                title = title,
                isExpanded = group.isExpanded,
                onClick = { onToggleGroup(group.key) },
            )
        }
        GroupTitle(
            label = label,
            title = title,
            count = group.movies.size,
            onClick = directorId?.let { id -> { onDirectorClick(id, title) } },
            modifier =
                Modifier
                    .weight(1f)
                    .then(
                        if (isDirectorHeading) {
                            Modifier.heightIn(min = Size.size56)
                        } else {
                            Modifier
                        },
                    ),
            startPadding =
                when {
                    showsExpandControl -> Size.size0
                    isDirectorHeading -> Spacings.spacing16
                    else -> Size.size0
                },
            endPadding = if (isDirectorHeading) Spacings.spacing8 else Size.size0,
        )
    }
}

@Composable
private fun ExpandToggle(
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val action =
        stringResource(
            if (isExpanded) SR.string.watched_group_collapse else SR.string.watched_group_expand,
            title,
        )
    val state =
        stringResource(
            if (isExpanded) SR.string.watched_group_expanded else SR.string.watched_group_collapsed,
        )
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) EXPANDED_ROTATION else 0f,
        label = "expandRotation",
    )
    IconButton(
        onClick = onClick,
        modifier =
            Modifier.size(Size.size48).semantics {
                contentDescription = action
                stateDescription = state
            },
    ) {
        Icon(
            imageVector = AppIcons.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.rotate(rotation),
        )
    }
}

@Composable
private fun GroupTitle(
    label: WatchedGroupLabel,
    title: String,
    count: Int,
    onClick: (() -> Unit)?,
    startPadding: Dp,
    endPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val openDirectorLabel = stringResource(SR.string.watched_group_open_director)
    val spokenTitle =
        if (label == WatchedGroupLabel.OtherTitles) {
            stringResource(SR.string.watched_group_other_titles_description)
        } else {
            null
        }
    Row(
        modifier =
            modifier
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            onClickLabel = openDirectorLabel,
                            role = Role.Button,
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    },
                ).padding(start = startPadding, end = endPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier =
                    Modifier
                        .weight(1f, fill = false)
                        .then(
                            if (spokenTitle != null) {
                                Modifier.semantics { contentDescription = spokenTitle }
                            } else {
                                Modifier
                            },
                        ),
            )
            Text(
                text = pluralStringResource(SR.plurals.watched_group_movie_count, count, count),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun WatchedMoviesUiState.showsCollectionControls(): Boolean =
    hasMovies == true &&
        (screenState is WatchedScreenState.Content || screenState is WatchedScreenState.NoResults)

@Composable
private fun WatchedGroupLabel.asTitle(): String =
    when (this) {
        is WatchedGroupLabel.Text -> value
        is WatchedGroupLabel.Decade ->
            stringResource(
                SR.string.watched_group_decade_label,
                startYear,
            )
        WatchedGroupLabel.OtherTitles -> stringResource(SR.string.watched_group_other_titles)
        WatchedGroupLabel.UnknownDirector ->
            stringResource(
                SR.string.watched_group_unknown_director,
            )
        WatchedGroupLabel.NoGenre -> stringResource(SR.string.watched_group_no_genre)
        WatchedGroupLabel.Undated -> stringResource(SR.string.watched_group_undated)
    }

private const val KEY_SEPARATOR = ":"
private const val EXPANDED_ROTATION = 180f
private const val NUMBER_OF_MOCKED_MOVIES = 6
private const val POSTER_RATIO = 2f / 3f
private const val CONTENT_TYPE_HEADER = "header"
private const val CONTENT_TYPE_MOVIE = "movie"
private const val CONTENT_TYPE_ROW = "row"
private const val SHELF_COLUMNS = 3

@PreviewLightDark
@Composable
private fun WatchedMoviesScreenPreview() {
    val sampleMovies =
        List(NUMBER_OF_MOCKED_MOVIES) { index ->
            MovieUi(
                id = index,
                title = "Sample Movie $index",
                posterPath = null,
            )
        }
    val groups =
        listOf(
            WatchedGroupUi(
                key = "recent",
                label = null,
                movies = sampleMovies,
            ),
        )

    PreviewContentFullSize {
        WatchedMoviesScreen(
            uiState =
                WatchedMoviesUiState(
                    screenState = WatchedScreenState.Content,
                    groups = groups,
                ),
            onProcess = {},
            onMovieClick = {},
            onDirectorClick = { _, _ -> },
        )
    }
}
