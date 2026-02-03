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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Shadow
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
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.designsystem.theme.Weights
import com.asensiodev.feature.moviedetail.impl.presentation.component.GenreChip
import com.asensiodev.feature.moviedetail.impl.presentation.model.CastMemberUi
import com.asensiodev.feature.moviedetail.impl.presentation.model.GenreUi
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi
import java.util.Locale
import com.asensiodev.santoro.core.designsystem.R as DR
import com.asensiodev.santoro.core.stringresources.R as SR

private const val GOLD_COLOR = 0xFFFFC107
private const val POSTER_RATIO = 2f / 3f
private val POSTER_WIDTH = Size.size100
private val HEADER_HEIGHT = 280.dp
private val BACKDROP_HEIGHT = 250.dp
private const val PARALLAX_FACTOR = 0.5f
private const val MOVIE_ID = 12
private const val WORDS = 40
private const val YEAR_INDEX = 0
private const val DATE_DELIMITER = "-"
private const val COLLAPSE_THRESHOLD_FACTOR = 0.6f
private const val FULLY_COLLAPSED_AT_FACTOR = 1.2f
private const val APP_BAR_TITLE_ALPHA_OFFSET = 0.5f
private const val MOVIE_RUNTIME_MINUTES = 115
private const val GENRE_ACTION_ID = 1
private const val GENRE_ADVENTURE_ID = 2
private const val GENRE_DRAMA_ID = 3
private const val GENRE_COMEDY_ID = 4

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

@OptIn(ExperimentalMaterial3Api::class)
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

    val collapseThreshold = HEADER_HEIGHT.value * COLLAPSE_THRESHOLD_FACTOR
    val fullyCollapsedAt = HEADER_HEIGHT.value * FULLY_COLLAPSED_AT_FACTOR
    val scrollProgress =
        (
            (scrollState.value - collapseThreshold) /
                (fullyCollapsedAt - collapseThreshold)
        ).coerceIn(0f, 1f)

    val appBarBackgroundAlpha = scrollProgress
    val appBarTitleAlpha = ((scrollProgress - APP_BAR_TITLE_ALPHA_OFFSET) * 2f).coerceIn(0f, 1f)

    Box(modifier = modifier.fillMaxSize()) {
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
                    scrollState = scrollState,
                )
            }
        }

        CollapsibleTopAppBar(
            title = uiState.movie?.title.orEmpty(),
            backgroundAlpha = appBarBackgroundAlpha,
            titleAlpha = appBarTitleAlpha,
            onBackClicked = onBackClicked,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsibleTopAppBar(
    title: String,
    backgroundAlpha: Float,
    titleAlpha: Float,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surface.copy(alpha = backgroundAlpha),
        animationSpec = tween(durationMillis = 150),
        label = "appBarBackground",
    )

    val iconTint by animateColorAsState(
        targetValue =
            if (backgroundAlpha > 0.5f) {
                MaterialTheme.colorScheme.onSurface
            } else {
                Color.White
            },
        animationSpec = tween(durationMillis = 150),
        label = "iconTint",
    )

    val titleColor by animateColorAsState(
        targetValue =
            if (backgroundAlpha > 0.5f) {
                MaterialTheme.colorScheme.onSurface
            } else {
                Color.White
            },
        animationSpec = tween(durationMillis = 150),
        label = "titleColor",
    )

    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = titleColor,
                modifier = Modifier.graphicsLayer { alpha = titleAlpha },
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = null,
                    tint = iconTint,
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundColor,
                scrolledContainerColor = backgroundColor,
            ),
        modifier = modifier,
    )
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

    Box(modifier = modifier.fillMaxSize()) {
        ParallaxBackdrop(
            movie = movie,
            scrollValue = scrollState.value,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
        ) {
            MovieHeaderSection(movie = movie)

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
            ) {
                WatchlistActionsRow(
                    movie = movie,
                    onToggleWatchlist = onToggleWatchlist,
                    onToggleWatched = onToggleWatched,
                )
                Spacer(modifier = Modifier.height(Spacings.spacing8))
                MovieDetailsSection(movie = movie)
            }
        }
    }
}

@Composable
private fun ParallaxBackdrop(
    movie: MovieUi,
    scrollValue: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(BACKDROP_HEIGHT + 50.dp)
                .graphicsLayer {
                    translationY = -scrollValue * PARALLAX_FACTOR
                    alpha = 1f - (scrollValue / BACKDROP_HEIGHT.toPx()).coerceIn(0f, 1f)
                },
    ) {
        AsyncImage(
            model = movie.backdropPath ?: movie.posterPath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(DR.drawable.ic_movie_card_placeholder),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
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
    }
}

@Composable
private fun MovieHeaderSection(
    movie: MovieUi,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(HEADER_HEIGHT),
    ) {
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
                                Shadow(
                                    color = Color.Black,
                                    blurRadius = 4f,
                                ),
                        ),
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(Spacings.spacing8))
                MovieMetadataRow(movie = movie)
            }
        }
    }
}

@Composable
private fun MovieMetadataRow(
    movie: MovieUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
                imageVector = AppIcons.Star,
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

@Composable
private fun WatchlistActionsRow(
    movie: MovieUi,
    onToggleWatchlist: () -> Unit,
    onToggleWatched: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Spacings.spacing16, vertical = Spacings.spacing12),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        val watchlistContainerColor by animateColorAsState(
            targetValue =
                if (movie.isInWatchlist) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            animationSpec = tween(durationMillis = 300),
            label = "watchlistColor",
        )
        val watchlistContentColor by animateColorAsState(
            targetValue =
                if (movie.isInWatchlist) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
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
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            animationSpec = tween(durationMillis = 300),
            label = "watchedColor",
        )
        val watchedContentColor by animateColorAsState(
            targetValue =
                if (movie.isWatched) {
                    MaterialTheme.colorScheme.onSecondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            animationSpec = tween(durationMillis = 300),
            label = "watchedContentColor",
        )
        WatchedButton(onToggleWatched, watchedContainerColor, watchedContentColor, movie)
    }
}

@Composable
private fun MovieDetailsSection(
    movie: MovieUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = Spacings.spacing16),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
    ) {
        if (movie.genres.isNotEmpty()) {
            MovieGenresChipsSection(
                genres = movie.genres,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        val unknown = stringResource(SR.string.unknown_value)
        InfoRow(
            duration = movie.runtime ?: unknown,
            director = movie.director ?: unknown,
            country = movie.productionCountries.firstOrNull() ?: unknown,
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
                        stringResource(SR.string.default_no_description)
                    },
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 28.sp,
                    ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CastSection(cast = movie.cast)
        Spacer(modifier = Modifier.height(Spacings.spacing48))
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
        hasBorder = false,
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
                    if (isInWatchlist) AppIcons.Watchlist else AppIcons.WatchlistOutlined,
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
        hasBorder = false,
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
                        AppIcons.Watched
                    } else {
                        AppIcons.WatchedOutlined
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieGenresChipsSection(
    genres: List<GenreUi>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
    ) {
        genres.forEach { genre ->
            GenreChip(text = genre.name)
        }
    }
}

fun getYearFromDate(date: String?): String? = date?.split(DATE_DELIMITER)?.getOrNull(YEAR_INDEX)

@Composable
fun InfoRow(
    duration: String,
    director: String,
    country: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InfoItem(
            icon = AppIcons.Duration,
            label = duration,
            modifier = Modifier.weight(Weights.W08),
        )
        VerticalDivider()
        InfoItem(
            icon = AppIcons.Director,
            label = director,
            modifier = Modifier.weight(Weights.W12),
        )
        VerticalDivider()
        InfoItem(
            icon = AppIcons.Country,
            label = country,
            modifier = Modifier.weight(Weights.W10),
        )
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier =
            Modifier
                .width(Size.size1)
                .fillMaxHeight()
                .padding(vertical = Spacings.spacing4)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    )
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Size.size24),
        )
        Spacer(modifier = Modifier.height(Spacings.spacing8))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
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
    content: @Composable RowScope.() -> Unit,
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
                                    GenreUi(GENRE_ACTION_ID, "Action"),
                                    GenreUi(GENRE_ADVENTURE_ID, "Adventure"),
                                    GenreUi(GENRE_DRAMA_ID, "Drama"),
                                    GenreUi(GENRE_COMEDY_ID, "Comedy"),
                                    GenreUi(GENRE_COMEDY_ID, "Comedy"),
                                ),
                            productionCountries = listOf("USA", "Canada"),
                            cast = emptyList(),
                            runtime = "1h ${MOVIE_RUNTIME_MINUTES % 60}m",
                            director = "Gary Fleder",
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
