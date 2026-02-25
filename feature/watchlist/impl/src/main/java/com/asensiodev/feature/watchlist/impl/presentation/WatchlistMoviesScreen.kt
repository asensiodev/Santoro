package com.asensiodev.feature.watchlist.impl.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.noresultscontent.NoResultsContent
import com.asensiodev.core.designsystem.component.querytextfield.QueryTextField
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchlist.impl.presentation.component.WatchlistMovieItem
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun WatchlistMoviesRoute(
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchlistMoviesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WatchlistMoviesScreen(
        uiState = uiState,
        onQueryChanged = viewModel::updateQuery,
        onMovieClick = onMovieClick,
        onRemoveMovie = viewModel::onRemoveMovieClicked,
        onRemoveConfirmed = viewModel::onRemoveConfirmed,
        onRemoveDismissed = viewModel::onRemoveDismissed,
        modifier = modifier,
    )
}

@Composable
internal fun WatchlistMoviesScreen(
    uiState: WatchlistMoviesUiState,
    onQueryChanged: (String) -> Unit,
    onMovieClick: (Int) -> Unit,
    onRemoveMovie: (MovieUi) -> Unit,
    onRemoveConfirmed: () -> Unit,
    onRemoveDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            onQueryChanged = onQueryChanged,
        )
        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }

            uiState.errorMessage != null -> {
                ErrorContent(
                    message = stringResource(SR.string.error_message_retry),
                    onRetry = { onQueryChanged(uiState.query) },
                )
            }

            uiState.hasResults -> {
                MovieList(
                    movies = uiState.movies,
                    onMovieClick = onMovieClick,
                    onRemoveMovie = onRemoveMovie,
                )
            }

            else -> {
                NoResultsContent(
                    text = stringResource(SR.string.search_movies_no_search_results_text),
                )
            }
        }
    }

    uiState.movieToRemove?.let { movie ->
        ConfirmRemoveDialog(
            movieTitle = movie.title,
            onConfirm = onRemoveConfirmed,
            onDismiss = onRemoveDismissed,
        )
    }
}

@Composable
private fun MovieList(
    movies: List<MovieUi>,
    onMovieClick: (Int) -> Unit,
    onRemoveMovie: (MovieUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        modifier = modifier,
    ) {
        items(
            items = movies,
            key = { it.id },
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
    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.StartToEnd ||
                    value == SwipeToDismissBoxValue.EndToStart
                ) {
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
        backgroundContent = {
            val color by animateColorAsState(
                targetValue =
                    when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surfaceContainer
                        else -> Color(SWIPE_BACKGROUND_COLOR)
                    },
                label = "swipe_background_color",
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            color,
                            shape =
                                androidx.compose.foundation.shape.RoundedCornerShape(
                                    com.asensiodev.core.designsystem.theme.Size.size16,
                                ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        },
        content = { content() },
    )
}

@Composable
private fun ConfirmRemoveDialog(
    movieTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(SR.string.watchlist_remove_dialog_title))
        },
        text = {
            Text(text = stringResource(SR.string.watchlist_remove_dialog_body, movieTitle))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(SR.string.watchlist_remove_dialog_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(SR.string.watchlist_remove_dialog_cancel))
            }
        },
    )
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
                    movies = sampleMovies,
                    isLoading = false,
                    errorMessage = null,
                    hasResults = true,
                ),
            onQueryChanged = {},
            onMovieClick = {},
            onRemoveMovie = {},
            onRemoveConfirmed = {},
            onRemoveDismissed = {},
        )
    }
}

private const val MOVIE_SAMPLE_LIST_SIZE = 5
private const val SWIPE_BACKGROUND_COLOR = 0xFFB00020
