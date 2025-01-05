package com.asensiodev.feature.searchmovies.impl.presentation

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.asensiodev.core.designsystem.component.noresultscontent.NoResultsContent
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import javax.inject.Inject
import com.asensiodev.santoro.core.designsystem.R as DR
import com.asensiodev.santoro.core.stringresources.R as SR

class SearchMoviesScreen
    @Inject
    constructor() {
        @Composable
        fun Screen(onMovieClick: (Int) -> Unit) {
            SearchMoviesRoot(
                onMovieClick = onMovieClick,
            )
        }
    }

@Composable
internal fun SearchMoviesRoot(
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchMoviesViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    SearchMoviesScreen(
        uiState = uiState,
        onQueryChanged = viewModel::updateQuery,
        onMovieClick = onMovieClick,
        modifier = modifier,
    )
}

@Composable
internal fun SearchMoviesScreen(
    uiState: SearchMoviesUiState,
    onQueryChanged: (String) -> Unit,
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
        QueryTextField(uiState, onQueryChanged)
        if (uiState.query.isBlank()) {
            PopularMoviesContent(uiState, onMovieClick)
        } else {
            SearchMoviesContent(uiState, onQueryChanged, onMovieClick)
        }
    }
}

@Composable
private fun PopularMoviesContent(
    uiState: SearchMoviesUiState,
    onMovieClick: (Int) -> Unit,
) {
    when {
        uiState.isPopularMoviesLoading -> {
            LoadingIndicator()
        }

        uiState.errorMessage != null && !uiState.hasPopularMoviesResults -> {
            Text(
                text =
                    stringResource(
                        SR.string.search_movies_no_popular_movies_results_text,
                    ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        uiState.hasPopularMoviesResults -> {
            Text(
                text =
                    stringResource(
                        SR.string.search_movies_popular_movies_title,
                    ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            MovieList(
                movies = uiState.popularMovies,
                onMovieClick = onMovieClick,
            )
        }

        else -> {
            Text(
                text =
                    stringResource(
                        SR.string.search_movies_no_popular_movies_results_text,
                    ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SearchMoviesContent(
    uiState: SearchMoviesUiState,
    onQueryChanged: (String) -> Unit,
    onMovieClick: (Int) -> Unit,
) {
    when {
        uiState.isSearchLoading -> LoadingIndicator()
        uiState.errorMessage != null && !uiState.hasSearchResults -> {
            ErrorContent(
                message = stringResource(SR.string.search_movies_no_search_results_text),
                onRetry = { onQueryChanged(uiState.query) },
            )
        }

        uiState.hasSearchResults -> {
            MovieList(
                movies = uiState.searchMovieResults,
                onMovieClick = onMovieClick,
            )
        }

        else ->
            NoResultsContent(
                text = stringResource(SR.string.search_movies_no_search_results_text),
            )
    }
}

@Composable
private fun QueryTextField(
    uiState: SearchMoviesUiState,
    onQueryChanged: (String) -> Unit,
) {
    TextField(
        value = uiState.query,
        onValueChange = onQueryChanged,
        placeholder = { Text(stringResource(SR.string.search_movies_textfield_placeholder)) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            if (uiState.query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged(EMPTY_STRING) }) {
                    Icon(
                        imageVector = AppIcons.ClearIcon,
                        contentDescription =
                            stringResource(
                                SR.string.search_movies_query_text_field_clear_button_description,
                            ),
                    )
                }
            }
        },
    )
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
private fun SearchMoviesScreenPreview() {
    val sampleMovies =
        List(MOVIE_SAMPLE_LIST_SIZE) { index ->
            MovieUi(
                id = index,
                title = "Sample Movie $index",
                posterPath = null,
            )
        }

    PreviewContent {
        SearchMoviesScreen(
            uiState =
                SearchMoviesUiState(
                    query = "",
                    popularMovies = sampleMovies,
                    isSearchLoading = false,
                    errorMessage = null,
                    hasPopularMoviesResults = true,
                ),
            onQueryChanged = {},
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
private const val EMPTY_STRING = ""
