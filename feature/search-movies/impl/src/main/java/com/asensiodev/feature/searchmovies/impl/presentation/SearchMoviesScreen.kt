package com.asensiodev.feature.searchmovies.impl.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.domain.Movie
import javax.inject.Inject

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
    val uiState = viewModel.uiState.collectAsState().value

    SearchMoviesScreen(
        query = uiState.query,
        movies = uiState.movies,
        onQueryChanged = viewModel::updateQuery,
        onMovieClick = onMovieClick,
        modifier = modifier,
    )
}

@Composable
internal fun SearchMoviesScreen(
    query: String,
    movies: List<Movie>,
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
        // Search Bar
        TextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Search movies") },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(Size.size48),
        )

        Spacer(modifier = Modifier.height(Spacings.spacing16))

        // Movie List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
            contentPadding = PaddingValues(Spacings.spacing16),
        ) {
            items(movies) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                )
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(Size.size88),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(Spacings.spacing8)
                    .fillMaxSize(),
        ) {
            Image(
                painter = rememberAsyncImagePainter(movie.posterPath),
                contentDescription = movie.title,
                modifier =
                    Modifier
                        .size(Size.size64)
                        .padding(end = Spacings.spacing8),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchMoviesScreenPreview() {
    val sampleMovies =
        List(SAMPLE_QUERY_SIZE) { index ->
            Movie(
                id = index,
                title = "Sample Movie $index",
                overview = "This is a brief description of Sample Movie $index",
                posterPath = null,
                releaseDate = "releaseDate",
                popularity = 4.0,
                voteAverage = 4.0,
                voteCount = 4,
            )
        }

    SearchMoviesScreen(
        query = "",
        movies = sampleMovies,
        onQueryChanged = {},
        onMovieClick = {},
    )
}

@Preview(showBackground = true)
@Composable
fun MovieCardPreview() {
    val sampleMovie =
        Movie(
            id = 1,
            title = "Sample Movie",
            overview = "This is a brief description of Sample Movie",
            posterPath = null,
            releaseDate = "releaseDate",
            popularity = 4.0,
            voteAverage = 4.0,
            voteCount = 4,
        )

    MovieCard(
        movie = sampleMovie,
        onClick = {},
    )
}

private const val SAMPLE_QUERY_SIZE = 5
