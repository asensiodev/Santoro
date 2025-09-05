package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.topbar.SantoroAppBar
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.designsystem.theme.displayFontFamily
import com.asensiodev.feature.moviedetail.impl.presentation.component.AnimatedIconWithText
import com.asensiodev.feature.moviedetail.impl.presentation.component.GenreChip
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.designsystem.R as DR
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun MovieDetailRoute(
    movieId: Int,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(movieId) {
        viewModel.fetchMovieDetails(movieId)
    }

    MovieDetailScreen(
        uiState = uiState,
        onToggleWatchlist = viewModel::toggleWatchlist,
        onToggleWatched = viewModel::toggleWatched,
        onRetry = { viewModel.fetchMovieDetails(uiState.movie?.id ?: 0) },
        onBackClicked = onBackClicked,
        modifier = modifier,
    )
}

@Composable
internal fun MovieDetailScreen(
    uiState: MovieDetailUiState,
    onToggleWatchlist: () -> Unit,
    onToggleWatched: () -> Unit,
    onRetry: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    SantoroAppBar(
        title = stringResource(SR.string.movie_detail_top_bar_title),
        onBackClicked = onBackClicked,
    ) {
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.errorMessage != null ->
                ErrorContent(
                    message = stringResource(SR.string.error_message_retry),
                    onRetry = { onRetry() },
                )

            uiState.movie != null ->
                MovieDetailContent(
                    uiState = uiState,
                    onToggleWatchlist = onToggleWatchlist,
                    onToggleWatched = onToggleWatched,
                    modifier = modifier,
                    scrollState = scrollState,
                )
        }
    }
}

@Composable
internal fun MovieDetailContent(
    uiState: MovieDetailUiState,
    onToggleWatchlist: () -> Unit,
    onToggleWatched: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacings.spacing24)
                .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = uiState.movie?.posterPath,
            contentDescription = uiState.movie?.title ?: stringResource(SR.string.untitled_movie),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .aspectRatio(POSTER_ASPECT_RATIO)
                    .background(MaterialTheme.colorScheme.inverseOnSurface),
            contentScale = ContentScale.Fit,
            error = painterResource(DR.drawable.ic_movie_card_placeholder),
        )
        val genres = uiState.movie?.genres.orEmpty()
        if (genres.isNotEmpty()) {
            MovieGenresChipsSection(
                genres = genres,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacings.spacing4),
            )
        }
        Text(
            text = uiState.movie?.title ?: stringResource(SR.string.untitled_movie),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing16),
        ) {
            AdditionalMovieInfo(
                releaseDate = uiState.movie?.releaseDate,
                voteAverage = uiState.movie?.voteAverage,
                modifier = Modifier.weight(EQUAL_WEIGHT),
            )
            ListSavingButtons(
                isWatched = uiState.movie?.isWatched ?: false,
                isInWatchlist = uiState.movie?.isInWatchlist ?: false,
                onToggleWatched = onToggleWatched,
                onToggleWatchlist = onToggleWatchlist,
                modifier = Modifier.weight(EQUAL_WEIGHT),
            )
        }
        MovieOverview(uiState)
        Spacer(modifier = Modifier.height(Spacings.spacing16))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieGenresChipsSection(
    genres: List<String>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
    ) {
        genres.forEach { genre ->
            GenreChip(text = genre)
        }
    }
}

@Composable
private fun MovieOverview(uiState: MovieDetailUiState) {
    Text(
        text = uiState.movie?.overview ?: stringResource(SR.string.default_no_description),
        textAlign = TextAlign.Justify,
        modifier = Modifier.fillMaxWidth(),
        style =
            TextStyle(
                lineHeight = 1.5.em,
                fontSize = 18.sp,
                fontFamily = displayFontFamily,
                platformStyle =
                    PlatformTextStyle(
                        includeFontPadding = false,
                    ),
                hyphens = Hyphens.Auto,
                lineBreak = LineBreak.Paragraph,
            ),
    )
}

@Composable
private fun ListSavingButtons(
    isWatched: Boolean,
    isInWatchlist: Boolean,
    onToggleWatched: () -> Unit,
    onToggleWatchlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = modifier,
    ) {
        AnimatedIconWithText(
            isSelected = isWatched,
            onClick = onToggleWatched,
            selectedIcon = AppIcons.WatchedMoviesIcon,
            unselectedIcon = AppIcons.AddIcon,
            label = stringResource(SR.string.watched_icon_button),
        )
        Spacer(modifier = Modifier.width(Spacings.spacing16))
        AnimatedIconWithText(
            isSelected = isInWatchlist,
            onClick = onToggleWatchlist,
            selectedIcon = AppIcons.WatchlistIcon,
            unselectedIcon = AppIcons.AddIcon,
            label = stringResource(SR.string.watchlist_icon_button),
        )
    }
}

@Composable
private fun AdditionalMovieInfo(
    releaseDate: String?,
    voteAverage: Double?,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier,
    ) {
        if (!releaseDate.isNullOrEmpty()) {
            Text(
                text = getYearFromDate(releaseDate) ?: stringResource(SR.string.unknown_value),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Spacer(modifier = Modifier.height(Spacings.spacing8))
        if (voteAverage != null) {
            Text(
                text = stringResource(SR.string.rating, voteAverage),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

fun getYearFromDate(date: String?): String? = date?.split(DATE_DELIMITER)?.getOrNull(YEAR_INDEX)

@PreviewLightDark
@Composable
private fun MovieDetailScreenPreview() {
    PreviewContentFullSize {
        MovieDetailScreen(
            uiState =
                MovieDetailUiState(
                    isLoading = false,
                    movie =
                        MovieUi(
                            id = MOVIE_ID,
                            title = "Things To Do In Denver When You're Dead",
                            overview = LoremIpsum(WORDS).values.first(),
                            posterPath = null,
                            releaseDate = "2023-01-01",
                            popularity = 10.0,
                            voteAverage = 8.5,
                            voteCount = 1000,
                            genres =
                                listOf(
                                    "Action",
                                    "Adventure",
                                    "Drama",
                                    "Family",
                                    "Anime",
                                    "Comedy",
                                    "Terror",
                                    "Thriller",
                                    "Comedy",
                                    "Comedy",
                                ),
                            productionCountries = listOf("USA", "Canada"),
                            isWatched = false,
                            isInWatchlist = false,
                        ),
                    errorMessage = null,
                    hasResults = false,
                ),
            onToggleWatchlist = {},
            onToggleWatched = {},
            onRetry = {},
            onBackClicked = {},
        )
    }
}

private const val POSTER_ASPECT_RATIO = 2f / 2.5f
private const val MOVIE_ID = 12
private const val WORDS = 40
private const val EQUAL_WEIGHT = 0.5f
private const val YEAR_INDEX = 0
private const val DATE_DELIMITER = "-"
