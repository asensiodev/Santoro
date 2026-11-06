package com.asensiodev.feature.watchedmovies.impl.presentation.director

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun DirectorCollectionRoute(
    personId: Int,
    name: String,
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onDirectorProfileClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DirectorCollectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.process(DirectorCollectionIntent.Initialize(personId, name))
    }

    val onProcess = remember(viewModel) { viewModel::process }

    DirectorCollectionScreen(
        uiState = uiState,
        onProcess = onProcess,
        onBackClick = onBackClick,
        onMovieClick = onMovieClick,
        onDirectorProfileClick = { onDirectorProfileClick(personId) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DirectorCollectionScreen(
    uiState: DirectorCollectionUiState,
    onProcess: (DirectorCollectionIntent) -> Unit,
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onDirectorProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    DirectorName(
                        name = uiState.directorName,
                        profilePath = uiState.profilePath,
                        onClick = onDirectorProfileClick,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(AppIcons.ArrowBack, stringResource(SR.string.navigate_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
            contentPadding =
                PaddingValues(
                    start = Spacings.spacing16,
                    end = Spacings.spacing16,
                    top = Spacings.spacing8,
                    bottom = Spacings.spacing16,
                ),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            item(
                key = KEY_SUMMARY,
                contentType = KEY_SUMMARY,
            ) {
                CollectionSummary(uiState = uiState)
            }
            if (uiState.filmographyState == FilmographyState.ERROR) {
                item(
                    key = KEY_ERROR,
                    contentType = KEY_ERROR,
                ) {
                    FilmographyError(
                        onRetry = { onProcess(DirectorCollectionIntent.RetryFilmography) },
                    )
                }
            }
            item(
                key = KEY_SHOW_PENDING,
                contentType = KEY_SHOW_PENDING,
            ) {
                ShowPendingSwitch(
                    checked = uiState.showPending,
                    onCheckedChange = { onProcess(DirectorCollectionIntent.ChangeShowPending(it)) },
                )
            }
            if (uiState.visibleFilms.isEmpty() &&
                uiState.visibleShorts.isEmpty() &&
                uiState.filmographyState != FilmographyState.LOADING
            ) {
                item(
                    key = KEY_EMPTY,
                    contentType = KEY_EMPTY,
                ) {
                    Text(
                        text = stringResource(SR.string.director_collection_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = Spacings.spacing24),
                    )
                }
            }
            items(
                items = uiState.visibleFilms,
                key = { it.movieId },
                contentType = { CONTENT_TYPE_FILM },
            ) { film ->
                DirectorFilmRow(film = film, onClick = { onMovieClick(film.movieId) })
            }
            if (uiState.visibleShorts.isNotEmpty()) {
                item(
                    key = KEY_SHORTS,
                    contentType = KEY_SHORTS,
                ) {
                    ShortsHeading(watched = uiState.shortsWatchedCount, total = uiState.shorts.size)
                }
                items(
                    items = uiState.visibleShorts,
                    key = { "$KEY_SHORTS${it.movieId}" },
                    contentType = { CONTENT_TYPE_FILM },
                ) { film ->
                    DirectorFilmRow(film = film, onClick = { onMovieClick(film.movieId) })
                }
            }
        }
    }
}

@Composable
private fun ShortsHeading(
    watched: Int,
    total: Int,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = Spacings.spacing16, bottom = Spacings.spacing4)
                .semantics(mergeDescendants = true) { heading() },
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(SR.string.director_collection_shorts),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text =
                stringResource(
                    SR.string.director_collection_shorts_progress,
                    watched,
                    total,
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DirectorName(
    name: String,
    profilePath: String?,
    onClick: () -> Unit,
) {
    var photoFailed by remember(profilePath) { mutableStateOf(false) }
    Row(
        modifier =
            Modifier
                .heightIn(min = Size.size48)
                .clickable(
                    onClickLabel = stringResource(SR.string.director_collection_open_profile),
                    role = Role.Button,
                    onClick = onClick,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (profilePath != null && !photoFailed) {
            AsyncImage(
                model = BASE_PROFILE_URL + profilePath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { photoFailed = true },
                modifier =
                    Modifier
                        .padding(end = Spacings.spacing8)
                        .size(Size.size32)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag(DIRECTOR_PHOTO_TAG),
            )
        }
        Text(
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CollectionSummary(uiState: DirectorCollectionUiState) {
    val total = uiState.totalCount
    val percent = uiState.progressPercent
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    if (total != null) {
                        stringResource(
                            SR.string.director_collection_progress,
                            uiState.watchedCount,
                            total,
                        )
                    } else {
                        pluralStringResource(
                            SR.plurals.director_collection_watched_count,
                            uiState.watchedCount,
                            uiState.watchedCount,
                        )
                    },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (percent != null) {
                Text(
                    text = stringResource(SR.string.director_collection_percent, percent),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (uiState.filmographyState == FilmographyState.LOADING) {
                val loading = stringResource(SR.string.director_collection_loading)
                CircularProgressIndicator(
                    strokeWidth = Size.size2,
                    modifier =
                        Modifier
                            .size(Size.size24)
                            .semantics { contentDescription = loading },
                )
            }
        }
        if (percent != null) {
            LinearProgressIndicator(
                progress = { percent / PERCENT },
                strokeCap = StrokeCap.Round,
                gapSize = Size.size0,
                drawStopIndicator = {},
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        uiState.remainingCount?.let { remaining ->
            Text(
                text =
                    if (remaining == 0) {
                        stringResource(SR.string.director_collection_complete)
                    } else {
                        pluralStringResource(
                            SR.plurals.director_collection_remaining,
                            remaining,
                            remaining,
                        )
                    },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FilmographyError(onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(start = Spacings.spacing16, end = Spacings.spacing4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(SR.string.director_collection_error),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(SR.string.error_message_retry_button))
            }
        }
    }
}

@Composable
private fun ShowPendingSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = Size.size48)
                .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(SR.string.director_collection_show_pending),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = null)
    }
}

internal const val DIRECTOR_PHOTO_TAG = "director_photo"
private const val BASE_PROFILE_URL = "https://image.tmdb.org/t/p/w185"
private const val PERCENT = 100f
private const val KEY_SUMMARY = "summary"
private const val KEY_ERROR = "error"
private const val KEY_SHOW_PENDING = "show_pending"
private const val KEY_EMPTY = "empty"
private const val KEY_SHORTS = "shorts"
private const val CONTENT_TYPE_FILM = "film"
private const val PREVIEW_FIRST_YEAR = 2000

@PreviewLightDark
@Composable
private fun DirectorCollectionScreenPreview() {
    val films =
        listOf("Memento", "Inception", "Oppenheimer").mapIndexed { index, title ->
            DirectorFilm(
                index,
                title,
                null,
                "${PREVIEW_FIRST_YEAR + index}-01-01",
                isWatched =
                    index != 1,
            )
        }
    PreviewContentFullSize {
        DirectorCollectionScreen(
            uiState =
                DirectorCollectionUiState(
                    directorName = "Christopher Nolan",
                    films = films,
                    watchedCount = films.count { it.isWatched },
                    totalCount = films.size,
                    filmographyState = FilmographyState.CONTENT,
                ),
            onProcess = {},
            onBackClick = {},
            onMovieClick = {},
            onDirectorProfileClick = {},
        )
    }
}
