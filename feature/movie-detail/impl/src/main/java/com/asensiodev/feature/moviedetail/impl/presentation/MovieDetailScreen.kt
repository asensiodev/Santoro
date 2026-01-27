package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.component.topbar.SantoroAppBar
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.moviedetail.impl.presentation.component.GenreChip
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi
import java.util.Locale
import com.asensiodev.santoro.core.designsystem.R as DR
import com.asensiodev.santoro.core.stringresources.R as SR

private const val GOLD_COLOR = 0xFFFFC107

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
        title = "",
        onBackClicked = onBackClicked,
        backgroundColor = Color.Transparent,
    ) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }
            }

            uiState.errorMessage != null -> {
                ErrorContent(
                    message = stringResource(SR.string.error_message_retry),
                    onRetry = { onRetry() },
                )
            }

            uiState.movie != null -> {
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
}

@Composable
internal fun MovieDetailContent(
    uiState: MovieDetailUiState,
    onToggleWatchlist: () -> Unit,
    onToggleWatched: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
) {
    val movie = uiState.movie!!
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(HEADER_HEIGHT),
        ) {
            MoviePoster(movie)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(BACKDROP_HEIGHT)
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Black.copy(alpha = 0.7f),
                                            MaterialTheme.colorScheme.background,
                                        ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY,
                                ),
                        ),
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(horizontal = Spacings.spacing16),
                verticalAlignment = Alignment.Bottom,
            ) {
                AsyncImage(
                    model = movie.posterPath,
                    contentDescription = movie.title,
                    modifier =
                        Modifier
                            .width(POSTER_WIDTH)
                            .aspectRatio(POSTER_RATIO)
                            .padding(bottom = Spacings.spacing16)
                            .shadow(
                                elevation = Size.size8,
                                shape = MaterialTheme.shapes.small,
                            ).clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    error = painterResource(DR.drawable.ic_movie_card_placeholder),
                )
                Spacer(modifier = Modifier.width(Spacings.spacing16))
                Column(
                    modifier =
                        Modifier
                            .padding(bottom = Spacings.spacing24)
                            .weight(1f),
                ) {
                    Text(
                        text = movie.title,
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                shadow =
                                    androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black,
                                        blurRadius = 4f,
                                    ),
                            ),
                        color = Color.White,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(Spacings.spacing8))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!movie.releaseDate.isNullOrEmpty()) {
                            Text(
                                text = getYearFromDate(movie.releaseDate) ?: "",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            )
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                        if (movie.voteAverage > 0.0) {
                            Icon(
                                imageVector = AppIcons.WatchedMoviesIcon,
                                contentDescription = null,
                                tint = Color(GOLD_COLOR),
                                modifier = Modifier.size(Size.size16),
                            )
                            Spacer(modifier = Modifier.width(Size.size4))
                            Text(
                                text =
                                    String.format(
                                        Locale.getDefault(),
                                        "%.1f",
                                        movie.voteAverage,
                                    ),
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Action Buttons ---
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacings.spacing16, vertical = Spacings.spacing12),
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            val watchlistContainerColor by animateColorAsState(
                targetValue =
                    if (movie.isInWatchlist) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                animationSpec = tween(durationMillis = 300),
                label = "watchlistColor",
            )
            val watchlistContentColor by animateColorAsState(
                targetValue =
                    if (movie.isInWatchlist) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                animationSpec = tween(durationMillis = 300),
                label = "watchlistContentColor",
            )
            WatchlistButton(
                onToggleWatchlist,
                watchlistContainerColor,
                watchlistContentColor,
                movie,
            )
            val watchedContainerColor by animateColorAsState(
                targetValue =
                    if (movie.isWatched) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                animationSpec = tween(durationMillis = 300),
                label = "watchedColor",
            )
            val watchedContentColor by animateColorAsState(
                targetValue =
                    if (movie.isWatched) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                animationSpec = tween(durationMillis = 300),
                label = "watchedContentColor",
            )
            WatchedButton(onToggleWatched, watchedContainerColor, watchedContentColor, movie)
        }
        Column(
            modifier = Modifier.padding(horizontal = Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing24),
        ) {
            if (movie.genres.isNotEmpty()) {
                MovieGenresChipsSection(
                    genres = movie.genres,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            InfoRow(
                duration = "2h 15m",
                director = "Christopher Nolan",
                country = movie.productionCountries.firstOrNull() ?: "USA",
            )

            Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
                Text(
                    text = "Overview",
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                )
                Text(
                    text =
                        movie.overview.ifEmpty {
                            stringResource(
                                SR.string.default_no_description,
                            )
                        },
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 28.sp,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            CastSection(
                cast = getDummyCast(),
            )

            Spacer(modifier = Modifier.height(Spacings.spacing48))
        }
    }
}

@Composable
private fun RowScope.WatchlistButton(
    onToggleWatchlist: () -> Unit,
    watchlistContainerColor: Color,
    watchlistContentColor: Color,
    movie: MovieUi,
) {
    BounceButton(
        onClick = onToggleWatchlist,
        containerColor = watchlistContainerColor,
        contentColor = watchlistContentColor,
        hasBorder = !movie.isInWatchlist,
        modifier = Modifier.weight(1f),
    ) {
        AnimatedContent(
            targetState = movie.isInWatchlist,
            transitionSpec = {
                (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
            },
            label = "watchlistIcon",
        ) { isInWatchlist ->
            Icon(
                imageVector =
                    if (isInWatchlist) AppIcons.WatchlistIcon else AppIcons.AddIcon,
                contentDescription = null,
                modifier = Modifier.size(Size.size18),
            )
        }
        Spacer(modifier = Modifier.width(Spacings.spacing8))
        Text(
            text =
                if (movie.isInWatchlist) {
                    "Watchlist"
                } else {
                    "Watchlist"
                },
            maxLines = 1,
        )
    }
}

@Composable
private fun RowScope.WatchedButton(
    onToggleWatched: () -> Unit,
    watchedContainerColor: Color,
    watchedContentColor: Color,
    movie: MovieUi,
) {
    BounceButton(
        onClick = onToggleWatched,
        containerColor = watchedContainerColor,
        contentColor = watchedContentColor,
        hasBorder = !movie.isWatched,
        modifier = Modifier.weight(1f),
    ) {
        AnimatedContent(
            targetState = movie.isWatched,
            transitionSpec = {
                (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
            },
            label = "watchedIcon",
        ) { isWatched ->
            Icon(
                imageVector =
                    if (isWatched) {
                        AppIcons.WatchedMoviesIcon
                    } else {
                        AppIcons.WatchedMoviesIcon
                    },
                contentDescription = null,
                modifier = Modifier.size(Size.size18),
            )
        }
        Spacer(modifier = Modifier.width(Spacings.spacing8))
        Text(
            text =
                if (movie.isWatched) {
                    "Watched"
                } else {
                    "Watched"
                },
            maxLines = 1,
        )
    }
}

@Composable
private fun MoviePoster(movie: MovieUi) {
    AsyncImage(
        model = movie.backdropPath ?: movie.posterPath,
        contentDescription = null,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(BACKDROP_HEIGHT),
        contentScale = ContentScale.Crop,
        error = painterResource(DR.drawable.ic_movie_card_placeholder),
    )
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

private const val POSTER_RATIO = 2f / 3f
private val POSTER_WIDTH = Size.size100
private val HEADER_HEIGHT = 280.dp
private val BACKDROP_HEIGHT = 250.dp
private const val MOVIE_ID = 12
private const val WORDS = 40
private const val YEAR_INDEX = 0
private const val DATE_DELIMITER = "-"

@Composable
fun InfoRow(
    duration: String,
    director: String,
    country: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        InfoItem(icon = Icons.Rounded.DateRange, label = duration)
        InfoItem(icon = Icons.Rounded.Person, label = director)
        InfoItem(icon = Icons.Rounded.Place, label = country)
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Size.size24),
        )
        Spacer(modifier = Modifier.height(Spacings.spacing4))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CastSection(cast: List<CastMemberUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing12)) {
        Text(
            text = "Cast & Crew",
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing16),
            contentPadding = PaddingValues(horizontal = Spacings.spacing16),
        ) {
            items(cast) { actor ->
                CastMemberItem(actor)
            }
        }
    }
}

@Composable
fun CastMemberItem(actor: CastMemberUi) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(Size.size80),
    ) {
        AsyncImage(
            model = actor.profileUrl,
            contentDescription = actor.name,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(Size.size64)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            error = painterResource(id = DR.drawable.ic_launcher_foreground),
        )
        Spacer(modifier = Modifier.height(Spacings.spacing8))
        Text(
            text = actor.name,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = actor.character,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun BounceButton(
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    hasBorder: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "buttonScale",
    )

    Button(
        onClick = onClick,
        modifier =
            modifier
                .height(Size.size48)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        shape = CircleShape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        border =
            if (hasBorder) {
                androidx.compose.foundation.BorderStroke(
                    Size.size1,
                    MaterialTheme.colorScheme.outline,
                )
            } else {
                null
            },
        interactionSource = interactionSource,
        content = content,
    )
}

data class CastMemberUi(
    val name: String,
    val character: String,
    val profileUrl: String?,
)

fun getDummyCast(): List<CastMemberUi> =
    listOf(
        CastMemberUi(
            "Leonardo DiCaprio",
            "Cobb",
            "https://image.tmdb.org/t/p/w200/wo2hJpn04vbtmh0B9utCFdsQhxM.jpg",
        ),
        CastMemberUi(
            "Joseph Gordon-Levitt",
            "Arthur",
            "https://image.tmdb.org/t/p/w200/4X1wv5C5R1J1wN1wJ1wN1wN1wN1.jpg",
        ),
        CastMemberUi(
            "Ellen Page",
            "Ariadne",
            "https://image.tmdb.org/t/p/w200/4X1wv5C5R1J1wN1wJ1wN1wN1wN1.jpg",
        ),
        CastMemberUi(
            "Tom Hardy",
            "Eames",
            "https://image.tmdb.org/t/p/w200/4X1wv5C5R1J1wN1wJ1wN1wN1wN1.jpg",
        ),
        CastMemberUi("Ken Watanabe", "Saito", null),
        CastMemberUi("Cillian Murphy", "Robert Fischer", null),
    )
