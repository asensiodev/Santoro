package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.noresultscontent.NoResultsContent
import com.asensiodev.core.designsystem.component.querytextfield.QueryTextField
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.domain.model.WatchedViewMode
import com.asensiodev.feature.watchedmovies.impl.domain.model.WatchedStats
import com.asensiodev.feature.watchedmovies.impl.presentation.component.MovieCard
import com.asensiodev.feature.watchedmovies.impl.presentation.component.WatchedCollectionControls
import com.asensiodev.feature.watchedmovies.impl.presentation.component.WatchedMovieRow
import com.asensiodev.feature.watchedmovies.impl.presentation.component.WatchedStatsDashboard
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupLabel
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupUi
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun WatchedMoviesRoute(
    onMovieClick: (Int) -> Unit,
    onDirectorClick: (Int) -> Unit,
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
    onDirectorClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = Spacings.spacing16)
                .padding(top = Spacings.spacing16),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        QueryTextField(
            query = uiState.query,
            placeholder = stringResource(SR.string.watched_movies_textfield_placeholder),
            onQueryChanged = { onProcess(WatchedMoviesIntent.UpdateQuery(it)) },
        )
        if (uiState.showsCollectionControls()) {
            WatchedCollectionControls(
                viewMode = uiState.viewMode,
                grouping = uiState.grouping,
                onViewModeChange = { onProcess(WatchedMoviesIntent.ChangeViewMode(it)) },
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
                    stats = uiState.stats,
                    onMovieClick = onMovieClick,
                    onDirectorClick = onDirectorClick,
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
    stats: WatchedStats?,
    onMovieClick: (Int) -> Unit,
    onDirectorClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(SHELF_COLUMNS),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        modifier = modifier.fillMaxSize(),
    ) {
        item(
            span = { GridItemSpan(maxLineSpan) },
            contentType = CONTENT_TYPE_STATS,
        ) {
            if (stats != null) {
                WatchedStatsDashboard(stats = stats)
            }
        }

        groups.forEach { group ->
            item(
                key = group.key,
                span = { GridItemSpan(maxLineSpan) },
                contentType = CONTENT_TYPE_HEADER,
            ) {
                WatchedGroupHeading(group = group, onDirectorClick = onDirectorClick)
            }
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
    onDirectorClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val directorId = group.directorId
    val openDirectorLabel = stringResource(SR.string.watched_group_open_director)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = Size.size48)
                .then(
                    if (directorId != null) {
                        Modifier.clickable(
                            onClickLabel = openDirectorLabel,
                            role = Role.Button,
                            onClick = { onDirectorClick(directorId) },
                        )
                    } else {
                        Modifier
                    },
                ).padding(top = Spacings.spacing4),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = group.label.asTitle(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f, fill = false),
        )
        Text(
            text =
                pluralStringResource(
                    SR.plurals.watched_group_movie_count,
                    group.movies.size,
                    group.movies.size,
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (directorId != null) {
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
        WatchedGroupLabel.UnknownDate -> stringResource(SR.string.unknown_value)
        WatchedGroupLabel.UnknownDirector ->
            stringResource(
                SR.string.watched_group_unknown_director,
            )
        WatchedGroupLabel.NoGenre -> stringResource(SR.string.watched_group_no_genre)
        WatchedGroupLabel.Undated -> stringResource(SR.string.watched_group_undated)
    }

private const val KEY_SEPARATOR = ":"
private const val NUMBER_OF_MOCKED_MOVIES = 6
private const val POSTER_RATIO = 2f / 3f
private const val CONTENT_TYPE_STATS = "stats"
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
                watchedDate = "January 2024",
            )
        }
    val groups =
        listOf(
            WatchedGroupUi(
                key = "date:January 2024",
                label = WatchedGroupLabel.Text("January 2024"),
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
            onDirectorClick = {},
        )
    }
}
