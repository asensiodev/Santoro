package com.asensiodev.feature.watchedmovies.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.noresultscontent.NoResultsContent
import com.asensiodev.core.designsystem.component.querytextfield.QueryTextField
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchedmovies.impl.presentation.component.MovieCard
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun WatchedMoviesRoute(
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchedMoviesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WatchedMoviesScreen(
        uiState = uiState,
        onQueryChanged = viewModel::updateQuery,
        onMovieClick = onMovieClick,
        modifier = modifier,
    )
}

@Composable
internal fun WatchedMoviesScreen(
    uiState: WatchedMoviesUiState,
    onQueryChanged: (String) -> Unit,
    onMovieClick: (Int) -> Unit,
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
                WatchedMovieList(
                    movies = uiState.movies,
                    totalCount = uiState.totalWatchedMovies,
                    onMovieClick = onMovieClick,
                )
            }

            else -> {
                NoResultsContent(
                    text = stringResource(SR.string.watched_movies_no_results_text),
                )
            }
        }
    }
}

@Composable
fun WatchedMovieList(
    movies: Map<String, List<MovieUi>>,
    totalCount: Int,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = Size.size100),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        modifier = modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            GamificationHeader(totalCount = totalCount)
        }

        movies.forEach { (dateHeader, movieList) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                StickyHeader(title = dateHeader)
            }
            items(items = movieList, key = { it.id }) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                )
            }
        }
    }
}

@Composable
fun GamificationHeader(
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = Spacings.spacing8),
        shape = RoundedCornerShape(Size.size16),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Row(
            modifier = Modifier.padding(Spacings.spacing16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.width(Size.size32).height(Size.size32),
            )
            Spacer(modifier = Modifier.width(Spacings.spacing12))
            Column {
                Text(
                    text = "Total Watched",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                Text(
                    text = "$totalCount Movies",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
fun StickyHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = Spacings.spacing8),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private const val NUMBER_OF_MOCKED_MOVIES = 6

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
    val grouped = mapOf("January 2024" to sampleMovies)

    PreviewContentFullSize {
        WatchedMoviesScreen(
            uiState =
                WatchedMoviesUiState(
                    movies = grouped,
                    isLoading = false,
                    errorMessage = null,
                ),
            onQueryChanged = {},
            onMovieClick = {},
        )
    }
}
