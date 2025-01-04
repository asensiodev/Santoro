package com.asensiodev.feature.watchlist.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.asensiodev.core.designsystem.PreviewContent
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import javax.inject.Inject
import com.asensiodev.santoro.core.designsystem.R as DR
import com.asensiodev.santoro.core.stringresources.R as SR

class WatchlistMoviesScreen
    @Inject
    constructor() {
        @Composable
        fun Screen(onMovieClick: (Int) -> Unit) {
            WatchlistMoviesRoot(
                onMovieClick = onMovieClick,
            )
        }
    }

@Composable
internal fun WatchlistMoviesRoot(
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchlistMoviesViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    WatchlistMoviesScreen(
        uiState = uiState,
        onMovieClick = onMovieClick,
        modifier = modifier,
    )
}

@Composable
internal fun WatchlistMoviesScreen(
    uiState: WatchlistMoviesUiState,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacings.spacing16),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.errorMessage != null -> {
                ErrorContent(
                    message = stringResource(SR.string.search_movies_no_results_text),
                    onRetry = { },
                )
            }

            uiState.hasResults -> {
                MovieList(
                    movies = uiState.movies,
                    onMovieClick = onMovieClick,
                )
            }

            else -> NoResultsContent(modifier)
        }
    }
}

@Composable
fun MovieList(
    movies: List<MovieUi>,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = Size.size88),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        modifier = modifier,
    ) {
        items(movies) { movie ->
            MovieCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) },
            )
        }
    }
}

@Composable
private fun NoResultsContent(modifier: Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacings.spacing16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = stringResource(SR.string.search_movies_no_results_text),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun MovieCard(
    movie: MovieUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(Size.size128),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            if (movie.posterPath == null) {
                Icon(
                    painter = painterResource(DR.drawable.ic_movie_card_placeholder),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(Size.size160)
                            .weight(1f)
                            .padding(Spacings.spacing8),
                )
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(Spacings.spacing8),
                )
            } else {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(movie.posterPath)
                            .crossfade(true)
                            .build(),
                    contentDescription = movie.title,
                    // placeholder = painterResource(DR.drawable.ic_movie_card_placeholder),
                    error = painterResource(DR.drawable.ic_movie_card_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .size(Size.size160)
                            .clip(MaterialTheme.shapes.medium),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun WatchlistdMoviesScreenPreview() {
    val sampleMovies =
        List(MOVIE_SAMPLE_LIST_SIZE) { index ->
            MovieUi(
                id = index,
                title = "Sample Movie $index",
                posterPath = null,
            )
        }

    PreviewContent {
        WatchlistMoviesScreen(
            uiState =
                WatchlistMoviesUiState(
                    movies = sampleMovies,
                    isLoading = false,
                    errorMessage = null,
                    hasResults = true,
                ),
            onMovieClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun MovieCardPreview() {
    val sampleMovie =
        MovieUi(
            id = 1,
            title = "Sample Movie",
            posterPath = null,
        )

    PreviewContent {
        MovieCard(
            movie = sampleMovie,
            onClick = {},
        )
    }
}

private const val MOVIE_SAMPLE_LIST_SIZE = 5
