package com.asensiodev.feature.watchlist.impl.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import com.asensiodev.feature.watchlist.impl.presentation.component.WatchlistMovieItem
import com.asensiodev.feature.watchlist.impl.presentation.component.WatchlistSurpriseContent
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun WatchlistMoviesRoute(
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchlistMoviesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.process(WatchlistIntent.LoadMovies)
    }

    val onProcess = remember(viewModel) { viewModel::process }

    WatchlistMoviesScreen(
        uiState = uiState,
        onProcess = onProcess,
        onMovieClick = onMovieClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WatchlistMoviesScreen(
    uiState: WatchlistMoviesUiState,
    onProcess: (WatchlistIntent) -> Unit,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkSurface = colorScheme.surface.luminance() < colorScheme.onSurface.luminance()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacings.spacing16),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        QueryTextField(
            query = uiState.query,
            placeholder = stringResource(SR.string.watchlist_movies_textfield_placeholder),
            onQueryChanged = { onProcess(WatchlistIntent.UpdateQuery(it)) },
        )
        when (uiState.screenState) {
            is WatchlistScreenState.Loading -> {
                LoadingIndicator()
            }

            is WatchlistScreenState.Error -> {
                ErrorContent(
                    message = stringResource(SR.string.error_message_retry),
                    onRetry = { onProcess(WatchlistIntent.LoadMovies) },
                )
            }

            is WatchlistScreenState.Content -> {
                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0),
                    bottomBar = {
                        if (uiState.query.isBlank() && uiState.movies.size > 1) {
                            Box(
                                modifier =
                                    Modifier.fillMaxWidth().padding(
                                        top = Spacings.spacing12,
                                    ),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                FilledTonalButton(
                                    onClick = { onProcess(WatchlistIntent.SuggestMovie) },
                                    modifier = Modifier.heightIn(min = Size.size48),
                                    colors =
                                        ButtonDefaults.filledTonalButtonColors(
                                            containerColor =
                                                if (isDarkSurface) {
                                                    colorScheme.tertiaryContainer
                                                } else {
                                                    colorScheme.tertiary
                                                },
                                            contentColor =
                                                if (isDarkSurface) {
                                                    colorScheme.onTertiaryContainer
                                                } else {
                                                    colorScheme.onTertiary
                                                },
                                        ),
                                    elevation =
                                        ButtonDefaults.filledTonalButtonElevation(
                                            defaultElevation = Size.size4,
                                        ),
                                ) {
                                    Text(text = stringResource(SR.string.watchlist_surprise_action))
                                }
                            }
                        }
                    },
                ) { contentPadding ->
                    MovieList(
                        listHeader = uiState.listHeader,
                        movies = uiState.movies,
                        onMovieClick = onMovieClick,
                        onRemoveMovie = { onProcess(WatchlistIntent.RequestRemove(it)) },
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = contentPadding,
                    )
                }
            }

            is WatchlistScreenState.NoResults -> {
                NoResultsContent(
                    text = stringResource(SR.string.watchlist_search_no_results_text),
                    subtitle = stringResource(SR.string.watchlist_search_no_results_subtitle),
                )
            }

            is WatchlistScreenState.Empty -> {
                NoResultsContent(
                    text = stringResource(SR.string.watchlist_no_results_text),
                    subtitle = stringResource(SR.string.watchlist_no_results_subtitle),
                    icon = AppIcons.WatchlistOutlined,
                )
            }
        }
    }

    uiState.suggestedMovie?.let { movie ->
        ModalBottomSheet(
            onDismissRequest = { onProcess(WatchlistIntent.DismissSuggestion) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            WatchlistSurpriseContent(
                movie = movie,
                hasAlternatives = uiState.movies.size > 1,
                onViewDetails = {
                    onProcess(WatchlistIntent.DismissSuggestion)
                    onMovieClick(movie.id)
                },
                onPickAnother = { onProcess(WatchlistIntent.SuggestMovie) },
            )
        }
    }

    uiState.movieToRemove?.let { movie ->
        ConfirmRemoveDialog(
            movieTitle = movie.title,
            isRemoving = uiState.isRemovingMovie,
            hasError = uiState.hasRemoveError,
            onConfirm = { onProcess(WatchlistIntent.ConfirmRemove) },
            onDismiss = { onProcess(WatchlistIntent.DismissRemoveDialog) },
        )
    }
}

@Composable
private fun WatchlistListHeader(
    listHeader: WatchlistListHeaderUi,
    modifier: Modifier = Modifier,
) {
    val title =
        when (listHeader) {
            is WatchlistListHeaderUi.MoviesToWatch -> {
                pluralStringResource(
                    SR.plurals.watchlist_movies_to_watch_count,
                    listHeader.count,
                    listHeader.count,
                )
            }

            is WatchlistListHeaderUi.SearchResults -> {
                pluralStringResource(
                    SR.plurals.watchlist_search_results_count,
                    listHeader.count,
                    listHeader.count,
                )
            }
        }

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun MovieList(
    listHeader: WatchlistListHeaderUi?,
    movies: List<MovieUi>,
    onMovieClick: (Int) -> Unit,
    onRemoveMovie: (MovieUi) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        if (listHeader != null) {
            item(
                key = WATCHLIST_LIST_HEADER_KEY,
                contentType = CONTENT_TYPE_HEADER,
            ) {
                WatchlistListHeader(listHeader = listHeader)
            }
        }
        items(
            items = movies,
            key = { it.id },
            contentType = { CONTENT_TYPE_MOVIE },
        ) { movie ->
            SwipeToRemoveContainer(
                onSwiped = { onRemoveMovie(movie) },
            ) {
                WatchlistMovieItem(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToRemoveContainer(
    onSwiped: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.EndToStart) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSwiped()
                    false
                } else {
                    false
                }
            },
        )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isSwiping = dismissState.dismissDirection != SwipeToDismissBoxValue.Settled
            val color by animateColorAsState(
                targetValue =
                    if (isSwiping) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                label = "swipe_background_color",
            )
            val iconScale by animateFloatAsState(
                targetValue = if (isSwiping) 1f else 0.8f,
                label = "swipe_delete_icon_scale",
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            color,
                            shape = RoundedCornerShape(Size.size16),
                        ),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier =
                        Modifier
                            .padding(end = Spacings.spacing32)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                )
            }
        },
        content = { content() },
    )
}

@Composable
private fun ConfirmRemoveDialog(
    movieTitle: String,
    isRemoving: Boolean,
    hasError: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val bodyTemplate = stringResource(SR.string.watchlist_remove_dialog_body)
    val bodyText = stringResource(SR.string.watchlist_remove_dialog_body, movieTitle)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(SR.string.watchlist_remove_dialog_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
                Text(
                    text =
                        buildWatchlistRemoveDialogBody(
                            template = bodyTemplate,
                            formattedText = bodyText,
                            movieTitle = movieTitle,
                        ),
                )
                if (hasError) {
                    Text(
                        text = stringResource(SR.string.error_message_retry),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirm()
                },
                enabled = !isRemoving,
            ) {
                Text(
                    text = stringResource(SR.string.watchlist_remove_dialog_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isRemoving) {
                Text(text = stringResource(SR.string.watchlist_remove_dialog_cancel))
            }
        },
    )
}

private fun buildWatchlistRemoveDialogBody(
    template: String,
    formattedText: String,
    movieTitle: String,
) = buildAnnotatedString {
    val placeholderMatch = WATCHLIST_REMOVE_DIALOG_TITLE_PLACEHOLDER.find(template)
    if (placeholderMatch == null) {
        append(formattedText)
        return@buildAnnotatedString
    }

    append(template.substring(startIndex = 0, endIndex = placeholderMatch.range.first))
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append(movieTitle)
    }
    append(template.substring(startIndex = placeholderMatch.range.last + 1))
}

@PreviewLightDark
@Composable
private fun WatchlistMoviesScreenPreview() {
    val sampleMovies =
        List(MOVIE_SAMPLE_LIST_SIZE) { index ->
            MovieUi(
                id = index,
                title = "Sample Movie $index",
                posterPath = null,
                releaseYear = "2023",
                genres = "Action, Drama",
                rating = 8.5,
            )
        }

    PreviewContentFullSize {
        WatchlistMoviesScreen(
            uiState =
                WatchlistMoviesUiState(
                    screenState = WatchlistScreenState.Content,
                    listHeader = WatchlistListHeaderUi.MoviesToWatch(sampleMovies.size),
                    movies = sampleMovies,
                    totalMoviesCount = sampleMovies.size,
                ),
            onProcess = {},
            onMovieClick = {},
        )
    }
}

private const val MOVIE_SAMPLE_LIST_SIZE = 5
private const val CONTENT_TYPE_HEADER = "header"
private const val CONTENT_TYPE_MOVIE = "movie"
private const val WATCHLIST_LIST_HEADER_KEY = "watchlist_list_header"

private val WATCHLIST_REMOVE_DIALOG_TITLE_PLACEHOLDER = Regex("%(\\d+\\$)?s")
